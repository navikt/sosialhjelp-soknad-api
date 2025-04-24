package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.config.CustomCacheErrorHandler
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrCacheConfig
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrClient
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrService
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import java.util.UUID

class KrrCacheTest : AbstractCacheTest(KrrCacheConfig.CACHE_NAME) {
    @Autowired
    private lateinit var krrService: KrrService

    @MockkBean
    private lateinit var krrClient: KrrClient

    @MockkBean
    private lateinit var personIdService: SoknadServiceImpl

    @BeforeEach
    fun personIdMock() {
        every { personIdService.findPersonId(soknadId) } returns PERSON_ID
    }

    @Test
    override fun `Verdi skal lagres i cache`() {
        every { krrClient.getDigitalKontaktinformasjon(PERSON_ID) } returns createDigitalKontaktinfo()

        krrService.getMobilnummer(soknadId).also {
            assertThat(it).isEqualTo(createDigitalKontaktinfo().mobiltelefonnummer)
        }

        cache.get(soknadId, String::class.java).also { assertThat(it).isEqualTo(createDigitalKontaktinfo().mobiltelefonnummer) }

        verify(exactly = 1) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }
    }

    @Test
    override fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(KrrCacheConfig.CACHE_NAME) } returns cache
        every { cache.get(soknadId) } throws RuntimeException("Noe feilet")
        every { krrClient.getDigitalKontaktinformasjon(PERSON_ID) } returns createDigitalKontaktinfo()

        krrService.getMobilnummer(soknadId).also {
            assertThat(it).isEqualTo(createDigitalKontaktinfo().mobiltelefonnummer)
        }

        verify(exactly = 1) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }
        verify(exactly = 1) { cache.get(soknadId) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCacheGetError(any(), cache, soknadId) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        cache.put(soknadId, createDigitalKontaktinfo().mobiltelefonnummer)

        krrService.getMobilnummer(soknadId).also {
            assertThat(it).isEqualTo(createDigitalKontaktinfo().mobiltelefonnummer)
        }

        cache.get(soknadId, String::class.java)
            .also { assertThat(it).isEqualTo(createDigitalKontaktinfo().mobiltelefonnummer) }

        verify(exactly = 0) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }
    }

    @Test
    override fun `Hvis put til cache feiler skal fortsatt innhentet verdi returneres`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(KrrCacheConfig.CACHE_NAME) } returns cache
        every { cache.put(soknadId, createDigitalKontaktinfo().mobiltelefonnummer) } throws RuntimeException("Noe feilet")
        every { krrClient.getDigitalKontaktinformasjon(PERSON_ID) } returns createDigitalKontaktinfo()

        krrService.getMobilnummer(soknadId).also {
            assertThat(it).isEqualTo(createDigitalKontaktinfo().mobiltelefonnummer)
        }

        cache.get(soknadId).also { assertThat(it).isNull() }

        verify(exactly = 1) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCachePutError(any(), cache, soknadId, createDigitalKontaktinfo().mobiltelefonnummer) }
        verify(exactly = 1) { cache.put(soknadId, createDigitalKontaktinfo().mobiltelefonnummer) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    fun `Null-verdier skal lagres i cache`() {
        every { krrClient.getDigitalKontaktinformasjon(PERSON_ID) } returns null

        krrService.getMobilnummer(soknadId).also { assertThat(it).isNull() }

        verify(exactly = 1) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }

        clearAllMocks()

        krrService.getMobilnummer(soknadId).also { assertThat(it).isNull() }

        verify(exactly = 0) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }

        // wrapper-value i cache
        cache.get(soknadId).also { assertThat(it).isNotNull }
        cache.get(soknadId, String::class.java).also { assertThat(it).isNull() }
    }

    private fun createDigitalKontaktinfo(nr: String = "98765432"): DigitalKontaktinformasjon {
        return DigitalKontaktinformasjon(
            personident = PERSON_ID,
            aktiv = true,
            kanVarsles = true,
            reservert = false,
            mobiltelefonnummer = nr,
        )
    }

    companion object {
        private val soknadId = UUID.randomUUID()
        private const val PERSON_ID = "12345612345"
    }
}

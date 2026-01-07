package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.config.CustomCacheErrorHandler
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.DigitalKontaktinformasjon
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KontaktInfoResponse
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrCacheConfig
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrClient
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
class KrrCacheTest : AbstractCacheTest() {
    @SpykBean
    private lateinit var cacheManager: CacheManager

    private val cache get() = cacheManager.getCache(KrrCacheConfig.CACHE_NAME)!!

    @Autowired
    private lateinit var krrService: KrrService

    @MockkBean
    private lateinit var krrClient: KrrClient

    @MockkBean
    private lateinit var personIdService: SoknadServiceImpl

    @BeforeEach
    fun personIdMock() {
        cache.clear()
        every { personIdService.findPersonId(soknadId) } returns PERSON_ID
    }

    @Test
    override fun `Verdi skal lagres i cache`() {
        every { krrClient.getDigitalKontaktinformasjon(PERSON_ID) } returns createDigitalKontaktinfo()

        krrService.getMobilnummer(soknadId).also { assertThat(it).isEqualTo(MOBILNUMMER) }
        verify(exactly = 1) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }

        krrService.getMobilnummer(soknadId).also { assertThat(it).isEqualTo(MOBILNUMMER) }
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
            assertThat(it).isEqualTo(MOBILNUMMER)
        }

        verify(exactly = 1) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }
        verify(exactly = 1) { cache.get(soknadId) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCacheGetError(any(), cache, soknadId) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        cache.put(soknadId, MOBILNUMMER)

        krrService.getMobilnummer(soknadId).also { assertThat(it).isEqualTo(MOBILNUMMER) }
        verify(exactly = 0) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }
    }

    @Test
    override fun `Hvis put til cache feiler skal fortsatt innhentet verdi returneres`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(KrrCacheConfig.CACHE_NAME) } returns cache
        every { cache.put(soknadId, MOBILNUMMER) } throws RuntimeException("Noe feilet")
        every { krrClient.getDigitalKontaktinformasjon(PERSON_ID) } returns createDigitalKontaktinfo()

        krrService.getMobilnummer(soknadId).also {
            assertThat(it).isEqualTo(MOBILNUMMER)
        }

        cache.get(soknadId).also { assertThat(it).isNull() }

        verify(exactly = 1) { krrClient.getDigitalKontaktinformasjon(PERSON_ID) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCachePutError(any(), cache, soknadId, MOBILNUMMER) }
        verify(exactly = 1) { cache.put(soknadId, MOBILNUMMER) }

        unmockkObject(CustomCacheErrorHandler)
    }

    private fun createDigitalKontaktinfo(nr: String = MOBILNUMMER): KontaktInfoResponse {
        return KontaktInfoResponse(
            personer =
                mapOf(
                    PERSON_ID to
                        DigitalKontaktinformasjon(
                            personident = PERSON_ID,
                            aktiv = true,
                            kanVarsles = true,
                            reservert = false,
                            mobiltelefonnummer = nr,
                        ),
                ),
            feil = null,
        )
    }

    companion object {
        private val soknadId = UUID.randomUUID()
        private const val PERSON_ID = "12345612345"
        private const val MOBILNUMMER = "43215678"
    }
}

package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.config.CustomCacheErrorHandler
import no.nav.sosialhjelp.soknad.kodeverk.BeskrivelseDto
import no.nav.sosialhjelp.soknad.kodeverk.BetydningDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkCacheConfiguration
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkClient
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn.KOMMUNER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import java.time.LocalDate

class KodeverkCacheTest : AbstractCacheTest(KodeverkCacheConfiguration.CACHE_NAME) {
    @MockkBean
    private lateinit var kodeverkClient: KodeverkClient

    @Autowired
    private lateinit var kodeverkService: KodeverkService

    @Test
    override fun `Verdi skal lagres i cache`() {
        every { kodeverkClient.hentKodeverk(KOMMUNER.value) } returns createKodeverkDtoForKommuner()

        kodeverkService.getKommunenavn(OSLO)
            .also { assertThat(it).isEqualTo("Oslo") }

        verify(exactly = 1) { kodeverkClient.hentKodeverk(KOMMUNER.value) }

        cache.get(KOMMUNER.value, Map::class.java)
            .also { assertThat(it[OSLO]).isEqualTo("Oslo") }
    }

    @Test
    override fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`() {
        val cache: Cache = spyk()
        every { cacheManager.getCache(KodeverkCacheConfiguration.CACHE_NAME) } returns cache
        every { cache.get(any()) } throws RuntimeException("Something wrong")
        every { kodeverkClient.hentKodeverk(KOMMUNER.value) } returns createKodeverkDtoForKommuner()
        mockkObject(CustomCacheErrorHandler)

        kodeverkService.getKommunenavn(OSLO)!!
            .also { assertThat(it).isEqualTo("Oslo") }

        verify(exactly = 1) { kodeverkClient.hentKodeverk(KOMMUNER.value) }
        verify(exactly = 1) { cache.get(any()) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCacheGetError(any(), cache, KOMMUNER.value) }

        unmockkObject(CustomCacheErrorHandler)
    }

    @Test
    override fun `Skal ikke hente fra client hvis verdi finnes i cache`() {
        cache.put("Kommuner", mapOf(OSLO to "Oslo"))

        kodeverkService.getKommunenavn(OSLO)!!
            .also { assertThat(it).isEqualTo("Oslo") }

        verify(exactly = 0) { kodeverkClient.hentKodeverk(any()) }
    }

    @Test
    override fun `Hvis put til cache feiler skal fortsatt innhentet verdi returneres`() {
        mockkObject(CustomCacheErrorHandler)
        val cache: Cache = spyk()
        every { cacheManager.getCache(KodeverkCacheConfiguration.CACHE_NAME) } returns cache
        every { cache.put(KOMMUNER.value, any<Map<String, String?>>()) } throws RuntimeException("Something wrong")
        every { kodeverkClient.hentKodeverk(KOMMUNER.value) } returns createKodeverkDtoForKommuner()

        kodeverkService.getKommunenavn(OSLO).also { assertThat(it).isEqualTo("Oslo") }

        verify { kodeverkClient.hentKodeverk(KOMMUNER.value) }
        verify(exactly = 1) { CustomCacheErrorHandler.handleCachePutError(any(), cache, KOMMUNER.value, any<Map<String, String?>>()) }
    }

    @Test
    fun `Error eller null i client skal ikke lagre noe i cache`() {
        every { kodeverkClient.hentKodeverk(any()) } throws RuntimeException()

        kodeverkService.getKommunenavn(OSLO).also { assertThat(it).isNull() }

        cache.get(KOMMUNER.value).also { assertThat(it).isNull() }
    }

    @Test
    fun `Feil i cachelag skal hente direkte fra Kodeverk og evicte key i cache`() {
        every { kodeverkClient.hentKodeverk(any()) } returns createKodeverkDtoForKommuner()
        cache.put(KOMMUNER.value, "Noe helt på trynet")

        kodeverkService.getKommunenavn(OSLO)!!.also { assertThat(it).isEqualTo("Oslo") }

        cache.get(KOMMUNER.value).also { assertThat(it).isNull() }
    }

    private fun createKodeverkDtoForKommuner() =
        KodeverkDto(
            betydninger =
                mapOf(
                    "0301" to createListOfBetydningDto(term = "Oslo"),
                    "3054" to createListOfBetydningDto(term = "Lunner"),
                    "5051" to createListOfBetydningDto(term = "Trondheim"),
                    "1151" to createListOfBetydningDto(term = "Utsira"),
                ),
        )

    private fun createListOfBetydningDto(term: String): List<BetydningDto> =
        listOf(
            BetydningDto(
                gyldigFra = LocalDate.now().minusYears(1),
                gyldigTil = LocalDate.now().plusYears(1),
                beskrivelser = mapOf("nb" to createBeskrivelseDto(term)),
            ),
        )

    private fun createBeskrivelseDto(term: String) = BeskrivelseDto(term = term, tekst = "Obsolete")

    companion object {
        private const val OSLO = "0301"
    }
}

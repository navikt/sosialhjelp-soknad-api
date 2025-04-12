package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.sosialhjelp.soknad.kodeverk.BeskrivelseDto
import no.nav.sosialhjelp.soknad.kodeverk.BetydningDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkClient
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.kodeverk.Kodeverksnavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class KodeverkCacheTest : AbstractCacheTest() {
    @MockkBean
    private lateinit var kodeverkClient: KodeverkClient

    @Autowired
    private lateinit var kodeverkService: KodeverkService

    @Test
    fun `Hente kodeverk skal lagres i cache`() {
        every { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER.value) } returns createKodeverkDtoForKommuner()

        kodeverkService.getKommunenavn(KOMMUNENUMMER_OSLO)
            .also { assertThat(it).isEqualTo("Oslo") }

        verify(exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER.value) }

        cacheManager.getCache("kodeverk")!!.get(Kodeverksnavn.KOMMUNER.value, Map::class.java)
            .also {
                assertThat(it[KOMMUNENUMMER_OSLO]).isEqualTo("Oslo")
            }
    }

    @Test
    fun `Skal ikke hente kodeverk hvis verdi ligger i cache`() {
        cacheManager.getCache("kodeverk")!!.put("Kommuner", mapOf(KOMMUNENUMMER_OSLO to "Oslo"))

        kodeverkService.getKommunenavn(KOMMUNENUMMER_OSLO)!!
            .also { assertThat(it).isEqualTo("Oslo") }

        verify(exactly = 0) { kodeverkClient.hentKodeverk(any()) }
    }

    @Test
    fun `Exception i cache-logikk skal ignoreres og vanlig logikk kjores`() {
        ValkeyContainer.stopContainer()

        every { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER.value) } returns createKodeverkDtoForKommuner()

        kodeverkService.getKommunenavn(KOMMUNENUMMER_OSLO)!!
            .also { assertThat(it).isEqualTo("Oslo") }

        verify(exactly = 1) { kodeverkClient.hentKodeverk(Kodeverksnavn.KOMMUNER.value) }

        ValkeyContainer.startContainer()
    }

    @Test
    fun `Error eller null i client skal ikke lagre noe i cache`() {
        every { kodeverkClient.hentKodeverk(any()) } throws RuntimeException()

        kodeverkService.getKommunenavn(KOMMUNENUMMER_OSLO).also { assertThat(it).isNull() }

        cacheManager.getCache("kodeverk")!!.get(Kodeverksnavn.KOMMUNER.value).also { assertThat(it).isNull() }
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
        private const val KOMMUNENUMMER_OSLO = "0301"
    }
}

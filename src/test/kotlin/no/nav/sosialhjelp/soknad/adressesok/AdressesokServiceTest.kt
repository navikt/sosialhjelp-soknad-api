package no.nav.sosialhjelp.soknad.adressesok

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

internal class AdressesokServiceTest {
    private val BYDELSNUMMER = "030101"
    private val KOMMUNENUMMER = "0301"
    private val KOMMUNENAVN = "OSLO"

    private val folkeregistretAdresse = JsonGateAdresse()
        .withGatenavn("Testveien")
        .withHusnummer("1")
        .withHusbokstav("B")
        .withPoststed("Oslo")

    private val adressesokClient = mockk<AdressesokClient>()
    private val kodeverkService = mockk<KodeverkService>()

    private val adressesokService = AdressesokService(adressesokClient, kodeverkService)

//    @Test
//    fun skalKasteFeil_AdresseSokResultErNull() {
//        every { adressesokClient.getAdressesokResult(any()) } returns null
//        assertThatExceptionOfType(RuntimeException::class.java)
//            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
//    }

    @Test
    fun skalKasteFeil_SokedataErNull() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(null) }
    }

    @Test
    fun skalKasteFeil_AdresseSokResultHitsErNull() {
        val adressesokResult = createAdressesokResultDto(null)
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_AdresseSokGirTomListe() {
        val adressesokResult = createAdressesokResultDto(emptyList())
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_AdresseSokGirFlereHits() {
        val adressesokResult = createAdressesokResultDto(
            java.util.List.of(
                AdressesokHitDto(vegadresseMedBydelsnummer()!!, 0.5f),
                AdressesokHitDto(vegadresseUtenBydelsnummer()!!, 0.7f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalReturnereAdresseForslagMedGeografiskTilknytningLikBydelsnummer() {
        val adressesokResult = createAdressesokResultDto(
            java.util.List.of(
                AdressesokHitDto(vegadresseMedBydelsnummer()!!, 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        val adresseForslag = adressesokService.getAdresseForslag(folkeregistretAdresse)
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(BYDELSNUMMER)
    }

    @Test
    fun skalReturnereAdresseForslagMedGeografiskTilknytningLikKommunenummer() {
        val adressesokResult = createAdressesokResultDto(
            java.util.List.of(
                AdressesokHitDto(vegadresseUtenBydelsnummer()!!, 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        val adresseForslag = adressesokService.getAdresseForslag(folkeregistretAdresse)
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(KOMMUNENUMMER)
    }

    @Test
    fun skalKasteFeil_flereHitsMedUlikeKommunenavn() {
        val adressesokResult = createAdressesokResultDto(
            java.util.List.of(
                AdressesokHitDto(vegadresse("kommune1", "0101", null)!!, 0.5f),
                AdressesokHitDto(vegadresse("kommune2", "0101", null)!!, 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_flereHitsMedUlikeKommunenummer() {
        val adressesokResult = createAdressesokResultDto(
            java.util.List.of(
                AdressesokHitDto(vegadresse("kommune", "1111", null)!!, 0.5f),
                AdressesokHitDto(vegadresse("kommune", "2222", null)!!, 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_flereHitsMedUlikeBydelsnummer() {
        val adressesokResult = createAdressesokResultDto(
            java.util.List.of(
                AdressesokHitDto(vegadresse("kommune", "1111", "030101")!!, 0.5f),
                AdressesokHitDto(vegadresse("kommune", "1111", "030102")!!, 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalReturnereAdresseForslagVedFlereHitsHvisDeHarSammeKommunenummerKommunenavnOgBydelsnummer() {
        val adressesokResult = createAdressesokResultDto(
            java.util.List.of(
                AdressesokHitDto(vegadresse("Oslo", "1111", "030101")!!, 0.5f),
                AdressesokHitDto(vegadresse("Oslo", "1111", "030101")!!, 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        val adresseForslag = adressesokService.getAdresseForslag(folkeregistretAdresse)
        assertThat(adresseForslag.kommunenavn).isEqualTo("Oslo")
        assertThat(adresseForslag.kommunenummer).isEqualTo("1111")
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo("030101")
    }

    private fun vegadresseMedBydelsnummer(): VegadresseDto? {
        return vegadresse(
            KOMMUNENAVN,
            KOMMUNENUMMER,
            BYDELSNUMMER
        )
    }

    private fun vegadresseUtenBydelsnummer(): VegadresseDto? {
        return vegadresse(KOMMUNENAVN, KOMMUNENUMMER, null)
    }

    private fun vegadresse(kommunenavn: String, kommunenummer: String, bydelsnummer: String?): VegadresseDto? {
        return VegadresseDto(
            "matrikkelId",
            1,
            "B",
            "Testveien",
            kommunenavn,
            kommunenummer,
            "0123",
            "Oslo",
            bydelsnummer
        )
    }

    private fun createAdressesokResultDto(hits: List<AdressesokHitDto>?): AdressesokResultDto {
        return AdressesokResultDto(hits, 1, 1, hits?.size ?: 0)
    }
}

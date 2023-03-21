package no.nav.sosialhjelp.soknad.adressesok

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

internal class AdressesokServiceTest {

    private val folkeregistretAdresse = JsonGateAdresse()
        .withGatenavn("Testveien")
        .withHusnummer("1")
        .withHusbokstav("B")
        .withPoststed("Oslo")

    private val adressesokClient = mockk<AdressesokClient>()
    private val kodeverkService = mockk<KodeverkService>()

    private val adressesokService = AdressesokService(adressesokClient, kodeverkService)

    private val resultDto = AdressesokResultDto(
        hits = emptyList(),
        pageNumber = 1,
        totalPages = 1,
        totalHits = 0
    )

    private val defaultVegadresse = VegadresseDto(
        matrikkelId = "matrikkelId",
        husnummer = 1,
        husbokstav = "B",
        adressenavn = "Testveien",
        kommunenavn = "Oslo",
        kommunenummer = "0301",
        postnummer = "0123",
        poststed = "Oslo",
        bydelsnummer = null
    )

    @Test
    fun skalKasteFeil_AdresseSokResultErNull() {
        every { adressesokClient.getAdressesokResult(any()) } returns null
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_AdresseSokResultHitsErNull() {
        val adressesokResult = resultDto.copy(hits = null)
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_AdresseSokGirTomListe() {
        val adressesokResult = resultDto.copy(hits = emptyList())
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_AdresseSokGirFlereHits() {
        val adressesokResult = resultDto.copy(
            hits = listOf(
                AdressesokHitDto(defaultVegadresse.copy(bydelsnummer = "030101"), 0.5f),
                AdressesokHitDto(defaultVegadresse, 0.7f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalReturnereAdresseForslagMedGeografiskTilknytningLikBydelsnummer() {
        val adressesokResult = resultDto.copy(
            hits = listOf(
                AdressesokHitDto(defaultVegadresse.copy(bydelsnummer = "030101"), 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        val adresseForslag = adressesokService.getAdresseForslag(folkeregistretAdresse)
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo("030101")
    }

    @Test
    fun skalReturnereAdresseForslagMedGeografiskTilknytningLikKommunenummer() {
        val adressesokResult = resultDto.copy(
            hits = listOf(
                AdressesokHitDto(defaultVegadresse, 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        val adresseForslag = adressesokService.getAdresseForslag(folkeregistretAdresse)
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo("0301")
    }

    @Test
    fun skalKasteFeil_flereHitsMedUlikeKommunenavn() {
        val adressesokResult = resultDto.copy(
            hits = listOf(
                AdressesokHitDto(defaultVegadresse.copy(kommunenavn = "kommune1"), 0.5f),
                AdressesokHitDto(defaultVegadresse.copy(kommunenavn = "kommune2"), 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_flereHitsMedUlikeKommunenummer() {
        val adressesokResult = resultDto.copy(
            hits = listOf(
                AdressesokHitDto(defaultVegadresse.copy(kommunenummer = "1111"), 0.5f),
                AdressesokHitDto(defaultVegadresse.copy(kommunenummer = "2222"), 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalKasteFeil_flereHitsMedUlikeBydelsnummer() {
        val adressesokResult = resultDto.copy(
            hits = listOf(
                AdressesokHitDto(defaultVegadresse.copy(bydelsnummer = "030101"), 0.5f),
                AdressesokHitDto(defaultVegadresse.copy(bydelsnummer = "030102"), 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { adressesokService.getAdresseForslag(folkeregistretAdresse) }
    }

    @Test
    fun skalReturnereAdresseForslagVedFlereHitsHvisDeHarSammeKommunenummerKommunenavnOgBydelsnummer() {
        val adressesokResult = resultDto.copy(
            hits = listOf(
                AdressesokHitDto(defaultVegadresse.copy(bydelsnummer = "030101"), 0.5f),
                AdressesokHitDto(defaultVegadresse.copy(bydelsnummer = "030101"), 0.5f)
            )
        )
        every { adressesokClient.getAdressesokResult(any()) } returns adressesokResult
        val adresseForslag = adressesokService.getAdresseForslag(folkeregistretAdresse)
        assertThat(adresseForslag.kommunenavn).isEqualTo("Oslo")
        assertThat(adresseForslag.kommunenummer).isEqualTo("0301")
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo("030101")
    }

    @Test
    fun `sokEtterAdresser skal returnere tom liste ved for kort sokestreng`() {
        assertThat(adressesokService.sokEtterAdresser("")).isEmpty()
        assertThat(adressesokService.sokEtterAdresser(" ")).isEmpty()
        assertThat(adressesokService.sokEtterAdresser("a")).isEmpty()
        assertThat(adressesokService.sokEtterAdresser("a 2")).isEmpty()
    }

    @Test
    fun `sokEtterAdresser skal returnere tom liste ved ingen treff i PDL`() {
        every { adressesokClient.getAdressesokResult(any()) } returns resultDto.copy(hits = null)
        assertThat(adressesokService.sokEtterAdresser("Oslogaten 2")).isEmpty()
    }

    @Test
    fun `sokEtterAdresser skal returnere funn fra PDL`() {
        every { adressesokClient.getAdressesokResult(any()) } returns resultDto.copy(
            hits = listOf(
                AdressesokHitDto(
                    vegadresse = defaultVegadresse,
                    score = 1.0f
                )
            ),
            pageNumber = 1,
            totalPages = 1,
            totalHits = 1
        )

        val result = adressesokService.sokEtterAdresser("oslogaten 42, 1337 Leet")

        assertThat(result).hasSize(1)
        assertThat(result.first().kommunenummer).isEqualTo("0301")
    }
}

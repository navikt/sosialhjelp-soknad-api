package no.nav.sosialhjelp.soknad.navenhet.finnadresse

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.json.createEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FinnAdresseServiceTest {
    companion object {
        private const val EIER = "123456789101"
        private const val KOMMUNENUMMER = "0300"
        private const val GEOGRAFISK_TILKNYTNING = "0101"
        private const val BYDEL = "0102"
        private const val GATEADRESSE = "gateadresse"
        private const val BOLIGNUMMER = "H0101"
        private const val GATENAVN = "Sandakerveien"
        private const val KOMMUNENAVN1 = "Kommune 1"
        private const val LANDKODE = "NOR"
        private const val POSTNUMMER = "0000"
        private const val POSTSTED = "Oslo"
        private const val HUSNUMMER = "53"
        private const val HUSBOKSTAV = "B"
    }

    private val adressesokService: AdressesokService = mockk()
    private val hentAdresseService: HentAdresseService = mockk()

    private val finnAdresseService = FinnAdresseService(adressesokService, hentAdresseService)

    @Test
    fun finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertGateadresse() {
        every { adressesokService.getAdresseForslag(any<JsonGateAdresse>()) } returns lagAdresseForslag()
        val json = createEmptyJsonInternalSoknad(EIER, false)
        val personalia = json.soknad.data.personalia
        personalia.folkeregistrertAdresse = createGateadresse()
        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT)
        assertThat(adresseForslag?.geografiskTilknytning).isEqualTo(GEOGRAFISK_TILKNYTNING)
        assertThat(adresseForslag?.kommunenummer).isEqualTo(KOMMUNENUMMER)
        assertThat(adresseForslag?.kommunenavn).isEqualTo(KOMMUNENAVN1)
        assertThat(adresseForslag?.type?.value).isEqualTo(GATEADRESSE)
    }

    @Test
    fun finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertMatrikkeladresse() {
        val json = createEmptyJsonInternalSoknad(EIER, false)
        val personalia = json.soknad.data.personalia
        personalia.folkeregistrertAdresse = createMatrikkeladresse()

        val matrikkelAdresse =
            KartverketMatrikkelAdresse(
                kommunenummer = KOMMUNENUMMER,
                gaardsnummer = "11",
                bruksnummer = "001",
                festenummer = "42",
                seksjonsnummer = "asd123",
                undernummer = null,
                bydelsnummer = "030107",
            )
        every { hentAdresseService.hentKartverketMatrikkelAdresseForInnloggetBruker() } returns matrikkelAdresse

        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT)
        assertThat(adresseForslag?.kommunenummer).isEqualTo(KOMMUNENUMMER)
        assertThat(adresseForslag?.type).isEqualTo(AdresseForslagType.MATRIKKELADRESSE)
    }

    @Test
    fun `finnAdresseFraSoknad returnerer null hvis hentAdresse ikke finner matrikkeladresse`() {
        val json = createEmptyJsonInternalSoknad(EIER, false)
        val personalia = json.soknad.data.personalia
        personalia.folkeregistrertAdresse = createMatrikkeladresse()

        every { hentAdresseService.hentKartverketMatrikkelAdresseForInnloggetBruker() } returns null

        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT)
        assertThat(adresseForslag).isNull()
    }

    @Test
    fun finnAdresseFraSoknadReturnererNullHvisAdresseValgMangler() {
        val json = createEmptyJsonInternalSoknad(EIER, false)
        val personalia = json.soknad.data.personalia
        personalia.oppholdsadresse = createGateadresse()
        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, null)
        assertThat(adresseForslag).isNull()
    }

    private fun createMatrikkeladresse(): JsonAdresse? =
        JsonMatrikkelAdresse()
            .withType(JsonAdresse.Type.MATRIKKELADRESSE)
            .withKommunenummer(KOMMUNENUMMER)

    private fun createGateadresse(): JsonAdresse? =
        JsonGateAdresse()
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode(LANDKODE)
            .withKommunenummer(KOMMUNENUMMER)
            .withPostnummer(POSTNUMMER)
            .withPoststed(POSTSTED)
            .withGatenavn(GATENAVN)
            .withHusnummer(HUSNUMMER)
            .withHusbokstav(HUSBOKSTAV)
            .withBolignummer(BOLIGNUMMER)

    private fun lagAdresseForslag(
        kommunenummer: String = KOMMUNENUMMER,
        kommunenavn: String = KOMMUNENAVN1,
        adresse: String = "Gateveien",
    ): AdresseForslag =
        AdresseForslag(
            adresse,
            null,
            null,
            kommunenummer,
            kommunenavn,
            "0030",
            "Mocka",
            GEOGRAFISK_TILKNYTNING,
            null,
            BYDEL,
            AdresseForslagType.GATEADRESSE,
        )
}

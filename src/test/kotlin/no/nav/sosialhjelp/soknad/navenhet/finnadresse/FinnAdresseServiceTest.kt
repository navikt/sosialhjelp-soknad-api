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
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FinnAdresseServiceTest {

    private val EIER = "123456789101"
    private val KOMMUNENUMMER = "0300"
    private val GEOGRAFISK_TILKNYTNING = "0101"
    private val BYDEL = "0102"
    private val GATEADRESSE = "gateadresse"
    private val BOLIGNUMMER = "H0101"
    private val GATENAVN = "Sandakerveien"
    private val KOMMUNENAVN1 = "Kommune 1"
    private val LANDKODE = "NOR"
    private val POSTNUMMER = "0000"
    private val POSTSTED = "Oslo"
    private val HUSNUMMER = "53"
    private val HUSBOKSTAV = "B"

    private val adressesokService = mockk<AdressesokService>()

    private val finnAdresseService = FinnAdresseService(adressesokService)

    @Test
    fun finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertGateadresse() {
        every { adressesokService.getAdresseForslag(any()) } returns lagAdresseForslag(KOMMUNENUMMER, KOMMUNENAVN1)
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val personalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        personalia.folkeregistrertAdresse = createGateadresse()
        val adresseForslagene = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString())
        assertThat(adresseForslagene).hasSize(1)
        val adresseForslag = adresseForslagene[0]
        assertThat(adresseForslag.geografiskTilknytning).isEqualTo(GEOGRAFISK_TILKNYTNING)
        assertThat(adresseForslag.kommunenummer).isEqualTo(KOMMUNENUMMER)
        assertThat(adresseForslag.kommunenavn).isEqualTo(KOMMUNENAVN1)
        assertThat(adresseForslag.type.value).isEqualTo(GATEADRESSE)
    }

    @Test
    fun finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertMatrikkeladresse() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val personalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        personalia.folkeregistrertAdresse = createMatrikkeladresse()
        val adresseForslagene = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString())
        assertThat(adresseForslagene).hasSize(1)
        val adresseForslag = adresseForslagene[0]
        assertThat(adresseForslag.kommunenummer).isEqualTo(KOMMUNENUMMER)
        assertThat(adresseForslag.type).isEqualTo(AdresseForslagType.MATRIKKELADRESSE)
        // Får kun kommunenummer som adresseforslag. Ut fra denne finner man navenhet i den lokale lista
    }

    @Test
    fun finnAdresseFraSoknadReturnererTomListeHvisAdresseValgMangler() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val personalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        personalia.oppholdsadresse = createGateadresse()
        val adresseForslagene = finnAdresseService.finnAdresseFraSoknad(personalia, null)
        assertThat(adresseForslagene).isEmpty()
    }

    private fun createMatrikkeladresse(): JsonAdresse? {
        return JsonMatrikkelAdresse()
            .withType(JsonAdresse.Type.MATRIKKELADRESSE)
            .withKommunenummer(KOMMUNENUMMER)
    }

    private fun createGateadresse(): JsonAdresse? {
        return JsonGateAdresse()
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode(LANDKODE)
            .withKommunenummer(KOMMUNENUMMER)
            .withPostnummer(POSTNUMMER)
            .withPoststed(POSTSTED)
            .withGatenavn(GATENAVN)
            .withHusnummer(HUSNUMMER)
            .withHusbokstav(HUSBOKSTAV)
            .withBolignummer(BOLIGNUMMER)
    }

    private fun lagAdresseForslag(kommunenummer: String, kommunenavn: String): AdresseForslag {
        return lagAdresseForslag(kommunenummer, kommunenavn, "Gateveien")
    }

    private fun lagAdresseForslag(kommunenummer: String, kommunenavn: String, adresse: String): AdresseForslag {
        return AdresseForslag(adresse, null, null, kommunenummer, kommunenavn, "0030", "Mocka", GEOGRAFISK_TILKNYTNING, null, BYDEL, AdresseForslagType.GATEADRESSE)
    }
}

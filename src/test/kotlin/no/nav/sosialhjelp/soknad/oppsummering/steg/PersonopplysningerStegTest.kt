package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PersonopplysningerStegTest {

    private val steg = PersonopplysningerSteg()

    private val navnUtenMellomnavn = JsonSokernavn().withFornavn("fornavn").withEtternavn("etternavn")
    private val navnMedMellomnavn = JsonSokernavn().withFornavn("fornavn").withMellomnavn("mellomnavn").withEtternavn("etternavn")
    private val kontonummerSystemdata = JsonKontonummer().withVerdi("12345678901").withKilde(JsonKilde.SYSTEM)
    private val telefonnummerSystemdata = JsonTelefonnummer().withVerdi("+4712345678").withKilde(JsonKilde.SYSTEM)
    private val folkeregGateadresse = JsonGateAdresse()
        .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
        .withType(JsonAdresse.Type.GATEADRESSE)
        .withGatenavn("gate")
        .withHusnummer("1")
        .withHusbokstav("B")
        .withPostnummer("0123")
        .withPoststed("poststed")

    @Test
    fun personalia_navnUtenMellomnavn() {
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val personaliaSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(personaliaSporsmal.felt).hasSize(3)
        validateFeltMedSvar(personaliaSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.TEKST, "fornavn etternavn")
        validateFeltMedSvar(personaliaSporsmal.felt!![1], Type.SYSTEMDATA, SvarType.TEKST, "11111111111")
        validateFeltMedSvar(personaliaSporsmal.felt!![2], Type.SYSTEMDATA, SvarType.TEKST, "NOR")
    }

    @Test
    fun personalia_navnMedMellomnavn_utenStatsborgerskap() {
        val soknad = createSoknad(navnMedMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse)
        soknad.soknad.data.personalia.statsborgerskap = null
        soknad.soknad.data.personalia.nordiskBorger = null

        val res = steg.get(soknad)

        val personaliaAvsnitt = res.avsnitt[0]
        val personaliaSporsmal = personaliaAvsnitt.sporsmal[0]
        assertThat(personaliaSporsmal.felt).hasSize(3)
        validateFeltMedSvar(personaliaSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.TEKST, "fornavn mellomnavn etternavn")
        validateFeltMedSvar(personaliaSporsmal.felt!![1], Type.SYSTEMDATA, SvarType.TEKST, "11111111111")
        validateFeltMedSvar(personaliaSporsmal.felt!![2], Type.SYSTEMDATA, SvarType.TEKST, null)
    }

    @Test
    fun folkeregistrertGateadresse() {
        val soknad =
            createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val adresseAvsnitt = res.avsnitt[1]
        assertThat(adresseAvsnitt.sporsmal).hasSize(1)

        val adresseSporsmal = adresseAvsnitt.sporsmal[0]
        assertThat(adresseSporsmal.felt).hasSize(1)

        val adresseFelt = adresseSporsmal.felt!![0]
        assertThat(adresseFelt.label).isEqualTo("kontakt.system.oppholdsadresse.folkeregistrertAdresse")
        validateFeltMedSvar(adresseFelt, Type.SYSTEMDATA, SvarType.TEKST, "gate 1B, 0123 poststed")
    }

    @Test
    fun folkeregistrertMatrikkeladresse() {
        val folkeregMatrikkeladresse = JsonMatrikkelAdresse()
            .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
            .withType(JsonAdresse.Type.MATRIKKELADRESSE)
            .withBruksnummer("bruksnummer")
            .withKommunenummer("kommunenr")
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregMatrikkeladresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val adresseAvsnitt = res.avsnitt[1]
        assertThat(adresseAvsnitt.sporsmal).hasSize(1)

        val adresseSporsmal = adresseAvsnitt.sporsmal[0]
        assertThat(adresseSporsmal.felt).hasSize(1)

        val adresseFelt = adresseSporsmal.felt!![0]
        assertThat(adresseFelt.label).isEqualTo("kontakt.system.oppholdsadresse.folkeregistrertAdresse")
        validateFeltMedSvar(adresseFelt, Type.SYSTEMDATA, SvarType.TEKST, "bruksnummer, kommunenr")
    }

    @Test
    fun midlertidigGateadresse() {
        val midlertidigGateadresse = JsonGateAdresse()
            .withAdresseValg(JsonAdresseValg.MIDLERTIDIG)
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withGatenavn("gate")
            .withHusnummer("1")
            .withPostnummer("0123")
            .withPoststed("poststed")
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, midlertidigGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val adresseAvsnitt = res.avsnitt[1]
        assertThat(adresseAvsnitt.sporsmal).hasSize(1)

        val adresseSporsmal = adresseAvsnitt.sporsmal[0]
        assertThat(adresseSporsmal.felt).hasSize(1)

        val adresseFelt = adresseSporsmal.felt!![0]
        assertThat(adresseFelt.label).isEqualTo("kontakt.system.oppholdsadresse.midlertidigAdresse")
        validateFeltMedSvar(adresseFelt, Type.SYSTEMDATA, SvarType.TEKST, "gate 1, 0123 poststed")
    }

    @Test
    fun adressesokGateadresse() {
        val adressesokGateadresse = JsonGateAdresse()
            .withAdresseValg(JsonAdresseValg.SOKNAD)
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withGatenavn("gate")
            .withHusnummer("1")
            .withPostnummer("0123")
            .withPoststed("poststed")
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, adressesokGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val adresseAvsnitt = res.avsnitt[1]
        assertThat(adresseAvsnitt.sporsmal).hasSize(1)

        val adresseSporsmal = adresseAvsnitt.sporsmal[0]
        assertThat(adresseSporsmal.felt).hasSize(1)

        val adresseFelt = adresseSporsmal.felt!![0]
        assertThat(adresseFelt.label).isEqualTo("kontakt.system.oppholdsadresse.valg.soknad")
        validateFeltMedSvar(adresseFelt, Type.TEKST, SvarType.TEKST, "gate 1, 0123 poststed")
    }

    @Test
    fun telefonnummerSystemdata() {
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val telefonnummerAvsnitt = res.avsnitt[2]
        assertThat(telefonnummerAvsnitt.sporsmal).hasSize(1)

        val telefonnummerSporsmal = telefonnummerAvsnitt.sporsmal[0]
        assertThat(telefonnummerSporsmal.erUtfylt).isTrue
        assertThat(telefonnummerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(telefonnummerSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.TEKST, telefonnummerSystemdata.verdi)
    }

    @Test
    fun telefonnummerBrukerUtfylt() {
        val telefonnummerBruker = JsonTelefonnummer()
            .withVerdi("+4712345678")
            .withKilde(JsonKilde.BRUKER)
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerBruker, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val telefonnummerAvsnitt = res.avsnitt[2]
        assertThat(telefonnummerAvsnitt.sporsmal).hasSize(1)

        val telefonnummerSporsmal = telefonnummerAvsnitt.sporsmal[0]
        assertThat(telefonnummerSporsmal.erUtfylt).isTrue
        assertThat(telefonnummerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(telefonnummerSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, telefonnummerBruker.verdi)
    }

    @Test
    fun telefonnummerIkkeUtfylt() {
        val ikkeUtfyltTelefonnummer = JsonTelefonnummer()
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, ikkeUtfyltTelefonnummer, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val telefonnummerAvsnitt = res.avsnitt[2]
        assertThat(telefonnummerAvsnitt.sporsmal).hasSize(1)

        val telefonnummerSporsmal = telefonnummerAvsnitt.sporsmal[0]
        assertThat(telefonnummerSporsmal.erUtfylt).isFalse
        assertThat(telefonnummerSporsmal.felt).isNull()
    }

    @Test
    fun kontonummerSystemdata() {
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse)
        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val kontonummerAvsnitt = res.avsnitt[3]
        assertThat(kontonummerAvsnitt.sporsmal).hasSize(1)

        val kontonummerSporsmal = kontonummerAvsnitt.sporsmal[0]
        assertThat(kontonummerSporsmal.erUtfylt).isTrue
        assertThat(kontonummerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(kontonummerSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.TEKST, kontonummerSystemdata.verdi)
    }

    @Test
    fun kontonummerBrukerUtfylt() {
        val kontonummerBruker = JsonKontonummer()
            .withVerdi("22222222222")
            .withKilde(JsonKilde.BRUKER)
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerBruker, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val kontonummerAvsnitt = res.avsnitt[3]
        assertThat(kontonummerAvsnitt.sporsmal).hasSize(1)

        val kontonummerSporsmal = kontonummerAvsnitt.sporsmal[0]
        assertThat(kontonummerSporsmal.erUtfylt).isTrue
        assertThat(kontonummerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(kontonummerSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, kontonummerBruker.verdi)
    }

    @Test
    fun harIkkeKontonummer() {
        val harIkkeKonto = JsonKontonummer().withHarIkkeKonto(true)
        val soknad = createSoknad(navnUtenMellomnavn, harIkkeKonto, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)

        val kontonummerAvsnitt = res.avsnitt[3]
        assertThat(kontonummerAvsnitt.sporsmal).hasSize(1)

        val kontonummerSporsmal = kontonummerAvsnitt.sporsmal[0]
        assertThat(kontonummerSporsmal.erUtfylt).isTrue
        assertThat(kontonummerSporsmal.felt).hasSize(1)
        validateFeltMedSvar(kontonummerSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "kontakt.kontonummer.harikke.true")
    }

    @Test
    fun kontonummerIkkeUtfylt() {
        val ikkeUtfylt = JsonKontonummer()
        val soknad = createSoknad(navnUtenMellomnavn, ikkeUtfylt, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)

        val kontonummerSporsmal = res.avsnitt[3].sporsmal[0]
        assertThat(kontonummerSporsmal.erUtfylt).isFalse
        assertThat(kontonummerSporsmal.felt).isNull()
    }

    private fun createSoknad(navn: JsonSokernavn, kontonummer: JsonKontonummer, telefonnummer: JsonTelefonnummer, oppholdsadresse: JsonAdresse): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withPersonalia(
                                JsonPersonalia()
                                    .withNavn(navn)
                                    .withPersonIdentifikator(JsonPersonIdentifikator().withVerdi("11111111111"))
                                    .withStatsborgerskap(JsonStatsborgerskap().withVerdi("NOR"))
                                    .withKontonummer(kontonummer)
                                    .withTelefonnummer(telefonnummer)
                                    .withOppholdsadresse(oppholdsadresse),
                            ),
                    ),
            )
    }
}

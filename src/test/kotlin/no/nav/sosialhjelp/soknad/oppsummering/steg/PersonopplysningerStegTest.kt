package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
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
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PersonopplysningerStegTest {
    private val steg = PersonopplysningerSteg

    private val navnUtenMellomnavn = JsonSokernavn(JsonSokernavn.Kilde.SYSTEM, "fornavn", "", "etternavn")
    private val navnMedMellomnavn = JsonSokernavn(JsonSokernavn.Kilde.SYSTEM, "fornavn", "mellomnavn", "etternavn")
    private val kontonummerSystemdata = JsonKontonummer(JsonKilde.SYSTEM, verdi = "12345678901")
    private val telefonnummerSystemdata = JsonTelefonnummer(JsonKilde.SYSTEM, "+4712345678")
    private val folkeregGateadresse =
        JsonGateAdresse(JsonKilde.SYSTEM, null, null, emptyList(), null, "0123", "poststed", "gate", "1", "B", JsonAdresseValg.FOLKEREGISTRERT)

    @Test
    fun personalia_navnUtenMellomnavn() {
        val soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(4)
        assertThat(res.avsnitt[0].sporsmal).hasSize(1)

        val personaliaSporsmal = res.avsnitt[0].sporsmal[0]
        assertThat(personaliaSporsmal.felt).hasSize(3)
        validateFeltMedSvar(personaliaSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.TEKST, "fornavn etternavn")
        validateFeltMedSvar(personaliaSporsmal.felt[1], Type.SYSTEMDATA, SvarType.TEKST, "11111111111")
        validateFeltMedSvar(personaliaSporsmal.felt[2], Type.SYSTEMDATA, SvarType.TEKST, "NOR")
    }

    @Test
    fun personalia_navnMedMellomnavn_utenStatsborgerskap() {
        val soknad = createSoknad(navnMedMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse, null)

        val res = steg.get(soknad)

        val personaliaAvsnitt = res.avsnitt[0]
        val personaliaSporsmal = personaliaAvsnitt.sporsmal[0]
        assertThat(personaliaSporsmal.felt).hasSize(3)
        validateFeltMedSvar(personaliaSporsmal.felt!![0], Type.SYSTEMDATA, SvarType.TEKST, "fornavn mellomnavn etternavn")
        validateFeltMedSvar(personaliaSporsmal.felt[1], Type.SYSTEMDATA, SvarType.TEKST, "11111111111")
        validateFeltMedSvar(personaliaSporsmal.felt[2], Type.SYSTEMDATA, SvarType.TEKST, null)
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
        val folkeregMatrikkeladresse =
            JsonMatrikkelAdresse(JsonKilde.SYSTEM, "kommunenr", null, "bruksnummer", null, null, null, JsonAdresseValg.FOLKEREGISTRERT)
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
        val midlertidigGateadresse =
            JsonGateAdresse(JsonKilde.SYSTEM, null, null, emptyList(), null, "0123", "poststed", "gate", "1", null, JsonAdresseValg.MIDLERTIDIG)
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
        val adressesokGateadresse =
            JsonGateAdresse(JsonKilde.BRUKER, null, null, emptyList(), null, "0123", "poststed", "gate", "1", null, JsonAdresseValg.SOKNAD)
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
        val telefonnummerBruker =
            JsonTelefonnummer(JsonKilde.BRUKER, "+4712345678")
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
        val ikkeUtfyltTelefonnummer = JsonTelefonnummer(JsonKilde.BRUKER, "")
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
        val kontonummerBruker =
            JsonKontonummer(JsonKilde.BRUKER, verdi = "22222222222")
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
        val harIkkeKonto = JsonKontonummer(JsonKilde.BRUKER, harIkkeKonto = true)
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
        val ikkeUtfylt = JsonKontonummer(JsonKilde.BRUKER)
        val soknad = createSoknad(navnUtenMellomnavn, ikkeUtfylt, telefonnummerSystemdata, folkeregGateadresse)

        val res = steg.get(soknad)

        val kontonummerSporsmal = res.avsnitt[3].sporsmal[0]
        assertThat(kontonummerSporsmal.erUtfylt).isFalse
        assertThat(kontonummerSporsmal.felt).isNull()
    }

    private fun createSoknad(
        navn: JsonSokernavn,
        kontonummer: JsonKontonummer,
        telefonnummer: JsonTelefonnummer,
        oppholdsadresse: JsonAdresse,
        statsborgerskap: JsonStatsborgerskap? = JsonStatsborgerskap(JsonKilde.SYSTEM, "NOR"),
    ): JsonInternalSoknad {
        return JsonInternalSoknad(
            soknad = JsonSoknad("", JsonData(personalia = JsonPersonalia(JsonPersonIdentifikator(JsonPersonIdentifikator.Kilde.SYSTEM, "11111111111"), navn, kontonummer, statsborgerskap, oppholdsadresse = oppholdsadresse, telefonnummer = telefonnummer), begrunnelse = no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad().soknad!!.data.begrunnelse, okonomi = no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad().soknad!!.data.okonomi), JsonSoknadsmottaker(), JsonDriftsinformasjon(false), emptyList()),
            vedlegg = JsonVedleggSpesifikasjon(emptyList()),
            mottaker = null,
            midlertidigAdresse = null,
        )
    }
}

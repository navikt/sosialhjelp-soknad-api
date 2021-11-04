package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class PersonopplysningerStegTest {

    private final PersonopplysningerSteg steg = new PersonopplysningerSteg();
    private final JsonSokernavn navnUtenMellomnavn = new JsonSokernavn().withFornavn("fornavn").withEtternavn("etternavn");
    private final JsonSokernavn navnMedMellomnavn = new JsonSokernavn().withFornavn("fornavn").withMellomnavn("mellomnavn").withEtternavn("etternavn");
    private final JsonKontonummer kontonummerSystemdata = new JsonKontonummer().withVerdi("12345678901").withKilde(JsonKilde.SYSTEM);
    private final JsonTelefonnummer telefonnummerSystemdata = new JsonTelefonnummer().withVerdi("+4712345678").withKilde(JsonKilde.SYSTEM);
    private final JsonGateAdresse folkeregGateadresse = new JsonGateAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT).withType(JsonAdresse.Type.GATEADRESSE).withGatenavn("gate").withHusnummer("1").withHusbokstav("B").withPostnummer("0123").withPoststed("poststed");

    @Test

    void personalia_navnUtenMellomnavn() {
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad);
        assertThat(res.getAvsnitt()).hasSize(4);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);

        var personaliaSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(personaliaSporsmal.getFelt()).hasSize(3);
        validateFeltMedSvar(personaliaSporsmal.getFelt().get(0), Type.SYSTEMDATA, SvarType.TEKST, "fornavn etternavn");
        validateFeltMedSvar(personaliaSporsmal.getFelt().get(1), Type.SYSTEMDATA, SvarType.TEKST, "11111111111");
        validateFeltMedSvar(personaliaSporsmal.getFelt().get(2), Type.SYSTEMDATA, SvarType.TEKST, "NOR");
    }

    @Test
    void personalia_navnMedMellomnavn_utenStatsborgerskap() {
        var soknad = createSoknad(navnMedMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);
        soknad.getSoknad().getData().getPersonalia().getStatsborgerskap().setVerdi(null);

        var res = steg.get(soknad);

        var personaliaAvsnitt = res.getAvsnitt().get(0);
        var personaliaSporsmal = personaliaAvsnitt.getSporsmal().get(0);
        assertThat(personaliaSporsmal.getFelt()).hasSize(3);
        validateFeltMedSvar(personaliaSporsmal.getFelt().get(0), Type.SYSTEMDATA, SvarType.TEKST, "fornavn mellomnavn etternavn");
        validateFeltMedSvar(personaliaSporsmal.getFelt().get(1), Type.SYSTEMDATA, SvarType.TEKST, "11111111111");
        validateFeltMedSvar(personaliaSporsmal.getFelt().get(2), Type.SYSTEMDATA, SvarType.TEKST, null);
    }

    @Test
    void folkeregistrertGateadresse() {
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var adresseAvsnitt = res.getAvsnitt().get(1);
        assertThat(adresseAvsnitt.getSporsmal()).hasSize(1);

        var adresseSporsmal = adresseAvsnitt.getSporsmal().get(0);
        assertThat(adresseSporsmal.getFelt()).hasSize(1);

        var adresseFelt = adresseSporsmal.getFelt().get(0);
        assertThat(adresseFelt.getLabel()).isEqualTo("kontakt.system.oppholdsadresse.folkeregistrertAdresse");
        validateFeltMedSvar(adresseFelt, Type.SYSTEMDATA, SvarType.TEKST, "gate 1B, 0123 poststed");
    }

    @Test
    void folkeregistrertMatrikkeladresse() {
        var folkeregMatrikkeladresse = new JsonMatrikkelAdresse()
                .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
                .withType(JsonAdresse.Type.MATRIKKELADRESSE)
                .withBruksnummer("bruksnummer")
                .withKommunenummer("kommunenr");
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregMatrikkeladresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var adresseAvsnitt = res.getAvsnitt().get(1);
        assertThat(adresseAvsnitt.getSporsmal()).hasSize(1);

        var adresseSporsmal = adresseAvsnitt.getSporsmal().get(0);
        assertThat(adresseSporsmal.getFelt()).hasSize(1);

        var adresseFelt = adresseSporsmal.getFelt().get(0);
        assertThat(adresseFelt.getLabel()).isEqualTo("kontakt.system.oppholdsadresse.folkeregistrertAdresse");
        validateFeltMedSvar(adresseFelt, Type.SYSTEMDATA, SvarType.TEKST, "bruksnummer, kommunenr");
    }

    @Test
    void midlertidigGateadresse() {
        var midlertidigGateadresse = new JsonGateAdresse()
                .withAdresseValg(JsonAdresseValg.MIDLERTIDIG)
                .withType(JsonAdresse.Type.GATEADRESSE)
                .withGatenavn("gate")
                .withHusnummer("1")
                .withPostnummer("0123")
                .withPoststed("poststed");
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, midlertidigGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var adresseAvsnitt = res.getAvsnitt().get(1);
        assertThat(adresseAvsnitt.getSporsmal()).hasSize(1);

        var adresseSporsmal = adresseAvsnitt.getSporsmal().get(0);
        assertThat(adresseSporsmal.getFelt()).hasSize(1);

        var adresseFelt = adresseSporsmal.getFelt().get(0);
        assertThat(adresseFelt.getLabel()).isEqualTo("kontakt.system.oppholdsadresse.midlertidigAdresse");
        validateFeltMedSvar(adresseFelt, Type.SYSTEMDATA, SvarType.TEKST, "gate 1, 0123 poststed");
    }

    @Test
    void adressesokGateadresse() {
        var adressesokGateadresse = new JsonGateAdresse()
                .withAdresseValg(JsonAdresseValg.SOKNAD)
                .withType(JsonAdresse.Type.GATEADRESSE)
                .withGatenavn("gate")
                .withHusnummer("1")
                .withPostnummer("0123")
                .withPoststed("poststed");
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, adressesokGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var adresseAvsnitt = res.getAvsnitt().get(1);
        assertThat(adresseAvsnitt.getSporsmal()).hasSize(1);

        var adresseSporsmal = adresseAvsnitt.getSporsmal().get(0);
        assertThat(adresseSporsmal.getFelt()).hasSize(1);

        var adresseFelt = adresseSporsmal.getFelt().get(0);
        assertThat(adresseFelt.getLabel()).isEqualTo("kontakt.system.oppholdsadresse.valg.soknad");
        validateFeltMedSvar(adresseFelt, Type.TEKST, SvarType.TEKST, "gate 1, 0123 poststed");
    }

    @Test
    void telefonnummerSystemdata() {
        var soknad1 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad1);

        assertThat(res.getAvsnitt()).hasSize(4);

        var telefonnummerAvsnitt = res.getAvsnitt().get(2);
        assertThat(telefonnummerAvsnitt.getSporsmal()).hasSize(1);

        var telefonnummerSporsmal = telefonnummerAvsnitt.getSporsmal().get(0);
        assertThat(telefonnummerSporsmal.getErUtfylt()).isTrue();
        assertThat(telefonnummerSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(telefonnummerSporsmal.getFelt().get(0), Type.SYSTEMDATA, SvarType.TEKST, telefonnummerSystemdata.getVerdi());
    }

    @Test
    void telefonnummerBrukerUtfylt() {
        var telefonnummerBruker = new JsonTelefonnummer()
                .withVerdi("+4712345678")
                .withKilde(JsonKilde.BRUKER);
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerBruker, folkeregGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var telefonnummerAvsnitt = res.getAvsnitt().get(2);
        assertThat(telefonnummerAvsnitt.getSporsmal()).hasSize(1);

        var telefonnummerSporsmal = telefonnummerAvsnitt.getSporsmal().get(0);
        assertThat(telefonnummerSporsmal.getErUtfylt()).isTrue();
        assertThat(telefonnummerSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(telefonnummerSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, telefonnummerBruker.getVerdi());
    }

    @Test
    void telefonnummerIkkeUtfylt() {
        var ikkeUtfyltTelefonnummer = new JsonTelefonnummer();
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, ikkeUtfyltTelefonnummer, folkeregGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var telefonnummerAvsnitt = res.getAvsnitt().get(2);
        assertThat(telefonnummerAvsnitt.getSporsmal()).hasSize(1);

        var telefonnummerSporsmal = telefonnummerAvsnitt.getSporsmal().get(0);
        assertThat(telefonnummerSporsmal.getErUtfylt()).isFalse();
        assertThat(telefonnummerSporsmal.getFelt()).isNull();
    }

    @Test
    void kontonummerSystemdata() {
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var kontonummerAvsnitt = res.getAvsnitt().get(3);
        assertThat(kontonummerAvsnitt.getSporsmal()).hasSize(1);

        var kontonummerSporsmal = kontonummerAvsnitt.getSporsmal().get(0);
        assertThat(kontonummerSporsmal.getErUtfylt()).isTrue();
        assertThat(kontonummerSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(kontonummerSporsmal.getFelt().get(0), Type.SYSTEMDATA, SvarType.TEKST, kontonummerSystemdata.getVerdi());
    }

    @Test
    void kontonummerBrukerUtfylt() {
        var kontonummerBruker = new JsonKontonummer()
                .withVerdi("22222222222")
                .withKilde(JsonKilde.BRUKER);
        var soknad = createSoknad(navnUtenMellomnavn, kontonummerBruker, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var kontonummerAvsnitt = res.getAvsnitt().get(3);
        assertThat(kontonummerAvsnitt.getSporsmal()).hasSize(1);

        var kontonummerSporsmal = kontonummerAvsnitt.getSporsmal().get(0);
        assertThat(kontonummerSporsmal.getErUtfylt()).isTrue();
        assertThat(kontonummerSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(kontonummerSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, kontonummerBruker.getVerdi());
    }

    @Test
    void harIkkeKontonummer() {
        var harIkkeKonto = new JsonKontonummer().withHarIkkeKonto(true);
        var soknad = createSoknad(navnUtenMellomnavn, harIkkeKonto, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(4);

        var kontonummerAvsnitt = res.getAvsnitt().get(3);
        assertThat(kontonummerAvsnitt.getSporsmal()).hasSize(1);

        var kontonummerSporsmal = kontonummerAvsnitt.getSporsmal().get(0);
        assertThat(kontonummerSporsmal.getErUtfylt()).isTrue();
        assertThat(kontonummerSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(kontonummerSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "kontakt.kontonummer.harikke.true");
    }

    @Test
    void kontonummerIkkeUtfylt() {
        var ikkeUtfylt = new JsonKontonummer();
        var soknad = createSoknad(navnUtenMellomnavn, ikkeUtfylt, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad);

        var kontonummerSporsmal = res.getAvsnitt().get(3).getSporsmal().get(0);
        assertThat(kontonummerSporsmal.getErUtfylt()).isFalse();
        assertThat(kontonummerSporsmal.getFelt()).isNull();
    }

    private JsonInternalSoknad createSoknad(JsonSokernavn navn, JsonKontonummer kontonummer, JsonTelefonnummer telefonnummer, JsonAdresse oppholdsadresse) {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withData(new JsonData()
                                .withPersonalia(new JsonPersonalia()
                                        .withNavn(navn)
                                        .withPersonIdentifikator(new JsonPersonIdentifikator().withVerdi("11111111111"))
                                        .withStatsborgerskap(new JsonStatsborgerskap().withVerdi("NOR"))
                                        .withKontonummer(kontonummer)
                                        .withTelefonnummer(telefonnummer)
                                        .withOppholdsadresse(oppholdsadresse)
                                )
                        )
                );
    }

}
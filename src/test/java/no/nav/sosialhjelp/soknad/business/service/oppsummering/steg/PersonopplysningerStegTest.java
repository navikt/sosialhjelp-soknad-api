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
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonopplysningerStegTest {

    private final PersonopplysningerSteg steg = new PersonopplysningerSteg();
    private final JsonSokernavn navnUtenMellomnavn = new JsonSokernavn().withFornavn("fornavn").withEtternavn("etternavn");
    private final JsonSokernavn navnMedMellomnavn = new JsonSokernavn().withFornavn("fornavn").withMellomnavn("mellomnavn").withEtternavn("etternavn");
    private final JsonKontonummer kontonummerSystemdata = new JsonKontonummer().withVerdi("12345678901").withKilde(JsonKilde.SYSTEM);
    private final JsonTelefonnummer telefonnummerSystemdata = new JsonTelefonnummer().withVerdi("+4712345678").withKilde(JsonKilde.SYSTEM);
    private final JsonGateAdresse folkeregGateadresse = new JsonGateAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT).withType(JsonAdresse.Type.GATEADRESSE).withGatenavn("gate").withHusnummer("1").withHusbokstav("B").withPostnummer("0123").withPoststed("poststed");

    @Test
    void personaliaAvsnitt() {
        // Navn uten mellomnavn
        var soknad1 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);

        var res = steg.get(soknad1);
        assertThat(res.getAvsnitt()).hasSize(4);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(1);
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt()).hasSize(3);
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("fornavn etternavn");
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA);
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(1).getSvar()).isEqualTo("11111111111");
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(1).getType()).isEqualTo(Type.SYSTEMDATA);
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(2).getSvar()).isEqualTo("NOR");
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(2).getType()).isEqualTo(Type.SYSTEMDATA);

        // Navn med mellomnavn. Statsborgerskap null
        var soknad2 = createSoknad(navnMedMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);
        soknad2.getSoknad().getData().getPersonalia().getStatsborgerskap().setVerdi(null);

        var res2 = steg.get(soknad2);
        assertThat(res2.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("fornavn mellomnavn etternavn");
        assertThat(res2.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(2).getSvar()).isNull();
    }

    @Test
    void adresseNavKontorAvsnitt() {
        // folkereg gateadresse
        var soknad1 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);
        var res = steg.get(soknad1);
        assertThat(res.getAvsnitt()).hasSize(4);
        assertThat(res.getAvsnitt().get(1).getSporsmal()).hasSize(1);
        assertThat(res.getAvsnitt().get(1).getSporsmal().get(0).getFelt()).hasSize(1);
        assertThat(res.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getLabel()).isEqualTo("kontakt.system.oppholdsadresse.folkeregistrertAdresse");
        assertThat(res.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("gate 1B, 0123 poststed");
        assertThat(res.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);

        // folkereg matrikkeladresse
        var folkeregMatrikkeladresse = new JsonMatrikkelAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT).withType(JsonAdresse.Type.MATRIKKELADRESSE).withBruksnummer("bruksnummer").withKommunenummer("kommunenr");
        var soknad2 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregMatrikkeladresse);
        var res2 = steg.get(soknad2);
        assertThat(res2.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getLabel()).isEqualTo("kontakt.system.oppholdsadresse.folkeregistrertAdresse");
        assertThat(res2.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("bruksnummer, kommunenr");
        assertThat(res2.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);

        // midlertidig gateadresse
        var midlertidigGateadresse = new JsonGateAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG).withType(JsonAdresse.Type.GATEADRESSE).withGatenavn("gate").withHusnummer("1").withPostnummer("0123").withPoststed("poststed");
        var soknad3 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, midlertidigGateadresse);
        var res3 = steg.get(soknad3);
        assertThat(res3.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getLabel()).isEqualTo("kontakt.system.oppholdsadresse.midlertidigAdresse");
        assertThat(res3.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("gate 1, 0123 poststed");
        assertThat(res3.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);

        // adressesok gateadresse
        var adressesokGateadresse = new JsonGateAdresse().withAdresseValg(JsonAdresseValg.SOKNAD).withType(JsonAdresse.Type.GATEADRESSE).withGatenavn("gate").withHusnummer("1").withPostnummer("0123").withPoststed("poststed");
        var soknad4 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, adressesokGateadresse);
        var res4 = steg.get(soknad4);
        assertThat(res4.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getLabel()).isEqualTo("kontakt.system.oppholdsadresse.valg.soknad");
        assertThat(res4.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("gate 1, 0123 poststed");
        assertThat(res4.getAvsnitt().get(1).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
    }

    @Test
    void telefonnummerAvsnitt() {
        // telefonnummer systemdata
        var soknad1 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);
        var res = steg.get(soknad1);
        assertThat(res.getAvsnitt()).hasSize(4);
        assertThat(res.getAvsnitt().get(2).getSporsmal()).hasSize(1);
        assertThat(res.getAvsnitt().get(2).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(res.getAvsnitt().get(2).getSporsmal().get(0).getFelt()).hasSize(1);
        assertThat(res.getAvsnitt().get(2).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo(telefonnummerSystemdata.getVerdi());
        assertThat(res.getAvsnitt().get(2).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA);

        // telefonnummer utfylt av bruker
        var telefonnummerBruker = new JsonTelefonnummer().withVerdi("+4712345678").withKilde(JsonKilde.BRUKER);
        var soknad2 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerBruker, folkeregGateadresse);
        var res2 = steg.get(soknad2);
        assertThat(res2.getAvsnitt().get(2).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(res2.getAvsnitt().get(2).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo(telefonnummerBruker.getVerdi());
        assertThat(res2.getAvsnitt().get(2).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.TEKST);

        // telefonnummer ikke utfylt
        var ikkeUtfyltTelefonnummer = new JsonTelefonnummer();
        var soknad3 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, ikkeUtfyltTelefonnummer, folkeregGateadresse);
        var res3 = steg.get(soknad3);
        assertThat(res3.getAvsnitt().get(2).getSporsmal().get(0).getErUtfylt()).isFalse();
        assertThat(res3.getAvsnitt().get(2).getSporsmal().get(0).getFelt()).isNull();
    }

    @Test
    void kontonummerAvsnitt() {
        // kontonummer systemdata
        var soknad1 = createSoknad(navnUtenMellomnavn, kontonummerSystemdata, telefonnummerSystemdata, folkeregGateadresse);
        var res = steg.get(soknad1);
        assertThat(res.getAvsnitt()).hasSize(4);
        assertThat(res.getAvsnitt().get(3).getSporsmal()).hasSize(1);
        assertThat(res.getAvsnitt().get(3).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(res.getAvsnitt().get(3).getSporsmal().get(0).getFelt()).hasSize(1);
        assertThat(res.getAvsnitt().get(3).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo(kontonummerSystemdata.getVerdi());
        assertThat(res.getAvsnitt().get(3).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.SYSTEMDATA);

        // kontonummer utfylt av bruker
        var kontonummerBruker = new JsonKontonummer().withVerdi("22222222222").withKilde(JsonKilde.BRUKER);
        var soknad2 = createSoknad(navnUtenMellomnavn, kontonummerBruker, telefonnummerSystemdata, folkeregGateadresse);
        var res2 = steg.get(soknad2);
        assertThat(res2.getAvsnitt().get(3).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(res2.getAvsnitt().get(3).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo(kontonummerBruker.getVerdi());
        assertThat(res2.getAvsnitt().get(3).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.TEKST);

        // harIkkeKontonummer
        var harIkkeKonto = new JsonKontonummer().withHarIkkeKonto(true);
        var soknad3 = createSoknad(navnUtenMellomnavn, harIkkeKonto, telefonnummerSystemdata, folkeregGateadresse);
        var res3 = steg.get(soknad3);
        assertThat(res3.getAvsnitt().get(3).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(res3.getAvsnitt().get(3).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("kontakt.kontonummer.harikke.true");
        assertThat(res3.getAvsnitt().get(3).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);

        // kontonummer ikke utfylt
        var ikkeUtfylt = new JsonKontonummer();
        var soknad4 = createSoknad(navnUtenMellomnavn, ikkeUtfylt, telefonnummerSystemdata, folkeregGateadresse);
        var res4 = steg.get(soknad4);
        assertThat(res4.getAvsnitt().get(3).getSporsmal().get(0).getErUtfylt()).isFalse();
        assertThat(res4.getAvsnitt().get(3).getSporsmal().get(0).getFelt()).isNull();
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
package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.finn.unleash.Unleash;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlService;
import no.nav.sosialhjelp.soknad.consumer.pdlperson.PersonSammenligner;
import no.nav.sosialhjelp.soknad.consumer.personv3.PersonServiceV3;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.Adresse;
import no.nav.sosialhjelp.soknad.domain.model.AdresserOgKontonummer;
import no.nav.sosialhjelp.soknad.domain.model.Bostedsadresse;
import no.nav.sosialhjelp.soknad.domain.model.Kontaktadresse;
import no.nav.sosialhjelp.soknad.domain.model.Matrikkeladresse;
import no.nav.sosialhjelp.soknad.domain.model.Person;
import no.nav.sosialhjelp.soknad.domain.model.Vegadresse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata.FEATURE_ADRESSER_PDL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdresseSystemdataTest {

    private static final String EIER = "12345678901";
    private static final Adresse GATEADRESSE = new Adresse();
    private static final Adresse MATRIKKEL_ADRESSE = new Adresse();
    private static final Adresse.Gateadresse STRUKTURERT_GATEADRESSE = new Adresse.Gateadresse();
    private static final Adresse.MatrikkelAdresse STRUKTURERT_MATRIKKEL_ADRESSE = new Adresse.MatrikkelAdresse();
    private static final Vegadresse DEFAULT_VEGADRESSE = new Vegadresse("gateveien", 1, "A", "", "0123", "poststed", "0301", "H0101");
    private static final Vegadresse ANNEN_VEGADRESSE = new Vegadresse("en annen sti", 32, null, null, "0456", "oslo", "0302", null);

    static {
        GATEADRESSE.setAdressetype("gateadresse");
        GATEADRESSE.setLandkode("NOR");
        STRUKTURERT_GATEADRESSE.gatenavn = "Gata mi";
        STRUKTURERT_GATEADRESSE.husbokstav = "A";
        STRUKTURERT_GATEADRESSE.bolignummer = "1";
        STRUKTURERT_GATEADRESSE.husnummer = "2";
        STRUKTURERT_GATEADRESSE.kommunenummer = "3";
        STRUKTURERT_GATEADRESSE.postnummer = "4";
        STRUKTURERT_GATEADRESSE.poststed = "Poststedet mitt";
        GATEADRESSE.setStrukturertAdresse(STRUKTURERT_GATEADRESSE);

        MATRIKKEL_ADRESSE.setAdressetype("matrikkeladresse");
        MATRIKKEL_ADRESSE.setLandkode("NOR");
        STRUKTURERT_MATRIKKEL_ADRESSE.eiendomsnavn = "Eiendomsnavnet mitt";
        STRUKTURERT_MATRIKKEL_ADRESSE.bolignummer = "1";
        STRUKTURERT_MATRIKKEL_ADRESSE.bruksnummer = "2";
        STRUKTURERT_MATRIKKEL_ADRESSE.festenummer = "3";
        STRUKTURERT_MATRIKKEL_ADRESSE.gaardsnummer = "4";
        STRUKTURERT_MATRIKKEL_ADRESSE.kommunenummer = "5";
        STRUKTURERT_MATRIKKEL_ADRESSE.postnummer = "6";
        STRUKTURERT_MATRIKKEL_ADRESSE.seksjonsnummer = "7";
        STRUKTURERT_MATRIKKEL_ADRESSE.undernummer = "8";
        STRUKTURERT_MATRIKKEL_ADRESSE.poststed = "Poststedet mitt";
        MATRIKKEL_ADRESSE.setStrukturertAdresse(STRUKTURERT_MATRIKKEL_ADRESSE);
    }

    @Mock
    private PersonServiceV3 personService;

    @Mock
    private PdlService pdlService;

    @Mock
    private PersonSammenligner personSammenligner;

    @Mock
    private Unleash unleashConsumer;

    @InjectMocks
    private AdresseSystemdata adresseSystemdata;

    @Test
    public void skalOppdatereFolkeregistrertAdresse() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer().withFolkeregistrertAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonAdresse folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThatGateAdresseIsCorrectlyConverted(GATEADRESSE, folkeregistrertAdresse);
    }

    @Test
    public void skalOppdatereOppholdsadresseOgPostAdresseMedFolkeregistrertAdresse() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer().withFolkeregistrertAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
        when(personService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonAdresse folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        JsonAdresse postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(postadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.equals(oppholdsadresse.withAdresseValg(null)), is(true));
        assertThat(folkeregistrertAdresse.equals(postadresse), is(true));
    }

    @Test
    public void skalIkkeOppdatereOppholdsadresseEllerPostAdresseDersomAdresseValgErNull() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer().withFolkeregistrertAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse())
                .withPostadresse(new JsonAdresse());
        when(personService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        JsonAdresse postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(postadresse.getAdresseValg(), nullValue());
        assertThat(postadresse.getType(), nullValue());
        assertThat(oppholdsadresse.getAdresseValg(), nullValue());
        assertThat(oppholdsadresse.getType(), nullValue());
    }

    @Test
    public void skalOppdatereOppholdsadresseOgPostAdresseMedMidlertidigAdresse() {
        AdresserOgKontonummer adresserOgKontonummer = new AdresserOgKontonummer()
                .withFolkeregistrertAdresse(MATRIKKEL_ADRESSE)
                .withMidlertidigAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG));
        when(personService.hentAddresserOgKontonummer(anyString())).thenReturn(adresserOgKontonummer);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonAdresse folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        JsonAdresse postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(postadresse.getKilde(), is(JsonKilde.SYSTEM));

        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.MATRIKKELADRESSE));
        assertThat(oppholdsadresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThat(postadresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThatMatrikkelAdresseIsCorrectlyConverted(MATRIKKEL_ADRESSE, folkeregistrertAdresse);
        assertThatGateAdresseIsCorrectlyConverted(GATEADRESSE, oppholdsadresse);
        assertThatGateAdresseIsCorrectlyConverted(GATEADRESSE, postadresse);
        assertThat(oppholdsadresse.equals(postadresse), is(true));
    }

    @Test
    public void skalOppdatereFolkeregistrertAdresse_vegadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));

        when(unleashConsumer.isEnabled(FEATURE_ADRESSER_PDL, false)).thenReturn(true);
        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var bostedsadresseVegadresse = personWithBostedsadresseVegadresse.getBostedsadresse().getVegadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse);

        verify(personService, times(0)).hentAddresserOgKontonummer(anyString());
    }

    @Test
    public void skalOppdatereFolkeregistrertAdresse_matrikkeladresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(unleashConsumer.isEnabled(FEATURE_ADRESSER_PDL, false)).thenReturn(true);
        var personWithBostedsadresseMatrikkeladresse = createPersonWithBostedsadresseMatrikkeladresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseMatrikkeladresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.MATRIKKELADRESSE));
        var matrikkeladresse = (JsonMatrikkelAdresse) folkeregistrertAdresse;
        var bostedsadresse = personWithBostedsadresseMatrikkeladresse.getBostedsadresse().getMatrikkeladresse();
        assertThat(matrikkeladresse.getBruksnummer(), is(bostedsadresse.getBruksenhetsnummer()));
        assertThat(matrikkeladresse.getKommunenummer(), is(bostedsadresse.getKommunenummer()));

        verify(personService, times(0)).hentAddresserOgKontonummer(anyString());
    }

    @Test
    public void skalOppdatereOppholdsadresseOgPostAdresseMedMidlertidigAdresse_kontaktadresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG));

        when(unleashConsumer.isEnabled(FEATURE_ADRESSER_PDL, false)).thenReturn(true);
        var personWithKontaktadresse = createPersonWithKontaktadresseVegadresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithKontaktadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(postadresse.getKilde(), is(JsonKilde.SYSTEM));

        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThat(oppholdsadresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThat(postadresse.getType(), is(JsonAdresse.Type.GATEADRESSE));

        var bostedsadresseVegadresse = personWithKontaktadresse.getBostedsadresse().getVegadresse();
        var kontaktadresseVegadresse = personWithKontaktadresse.getKontaktadresse().getVegadresse();

        assertThatVegadresseIsCorrectlyConverted(bostedsadresseVegadresse, folkeregistrertAdresse);
        assertThatVegadresseIsCorrectlyConverted(kontaktadresseVegadresse, oppholdsadresse);
        assertThatVegadresseIsCorrectlyConverted(kontaktadresseVegadresse, postadresse);

        verify(personService, times(0)).hentAddresserOgKontonummer(anyString());
    }

    @Test
    public void skalOppdatereOppholdsadresseOgPostAdresseMedFolkeregistrertAdresse_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));

        when(unleashConsumer.isEnabled(FEATURE_ADRESSER_PDL, false)).thenReturn(true);
        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();
        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(postadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.equals(oppholdsadresse.withAdresseValg(null)), is(true));
        assertThat(folkeregistrertAdresse.equals(postadresse), is(true));

        verify(personService, times(0)).hentAddresserOgKontonummer(anyString());
    }

    @Test
    public void skalIkkeOppdatereOppholdsadresseEllerPostAdresseDersomAdresseValgErNull_fraPdl() {
        var soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse())
                .withPostadresse(new JsonAdresse());

        when(unleashConsumer.isEnabled(FEATURE_ADRESSER_PDL, false)).thenReturn(true);
        var personWithBostedsadresseVegadresse = createPersonWithBostedsadresseVegadresse();
        when(pdlService.hentPerson(anyString())).thenReturn(personWithBostedsadresseVegadresse);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        var oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        var postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(postadresse.getAdresseValg(), nullValue());
        assertThat(postadresse.getType(), nullValue());
        assertThat(oppholdsadresse.getAdresseValg(), nullValue());
        assertThat(oppholdsadresse.getType(), nullValue());

        verify(personService, times(0)).hentAddresserOgKontonummer(anyString());
    }

    private void assertThatMatrikkelAdresseIsCorrectlyConverted(Adresse adresse, JsonAdresse jsonAdresse) {
        JsonMatrikkelAdresse matrikkelAdresse = (JsonMatrikkelAdresse) jsonAdresse;
        Adresse.MatrikkelAdresse strukturertAdresse = (Adresse.MatrikkelAdresse) adresse.getStrukturertAdresse();
        assertThat("bruksnummer", matrikkelAdresse.getBruksnummer(), is(strukturertAdresse.bruksnummer));
        assertThat("festenummer", matrikkelAdresse.getFestenummer(), is(strukturertAdresse.festenummer));
        assertThat("gaardsnummer", matrikkelAdresse.getGaardsnummer(), is(strukturertAdresse.gaardsnummer));
        assertThat("kommunenummer", matrikkelAdresse.getKommunenummer(), is(strukturertAdresse.kommunenummer));
        assertThat("seksjonsnummer", matrikkelAdresse.getSeksjonsnummer(), is(strukturertAdresse.seksjonsnummer));
        assertThat("undernummer", matrikkelAdresse.getUndernummer(), is(strukturertAdresse.undernummer));
    }

    private void assertThatGateAdresseIsCorrectlyConverted(Adresse adresse, JsonAdresse jsonAdresse) {
        JsonGateAdresse gateAdresse = (JsonGateAdresse) jsonAdresse;
        Adresse.Gateadresse strukturertAdresse = (Adresse.Gateadresse) adresse.getStrukturertAdresse();
        assertThat("bolignummer", gateAdresse.getBolignummer(), is(strukturertAdresse.bolignummer));
        assertThat("gatenavn", gateAdresse.getGatenavn(), is(strukturertAdresse.gatenavn));
        assertThat("husbokstav", gateAdresse.getHusbokstav(), is(strukturertAdresse.husbokstav));
        assertThat("husnummer", gateAdresse.getHusnummer(), is(strukturertAdresse.husnummer));
        assertThat("kommunenummer", gateAdresse.getKommunenummer(), is(strukturertAdresse.kommunenummer));
        assertThat("landkode", gateAdresse.getLandkode(), is(adresse.getLandkode()));
        assertThat("postnummer", gateAdresse.getPostnummer(), is(strukturertAdresse.postnummer));
        assertThat("poststed", gateAdresse.getPoststed(), is(strukturertAdresse.poststed));
    }

    private void assertThatVegadresseIsCorrectlyConverted(Vegadresse vegadresse, JsonAdresse jsonAdresse) {
        var gateAdresse = (JsonGateAdresse) jsonAdresse;
        assertThat(gateAdresse.getBolignummer(), is(vegadresse.getBruksenhetsnummer()));
        assertThat(gateAdresse.getGatenavn(), is(vegadresse.getAdressenavn()));
        assertThat(gateAdresse.getHusbokstav(), is(vegadresse.getHusbokstav()));
        assertThat(gateAdresse.getHusnummer(), is(vegadresse.getHusnummer().toString()));
        assertThat(gateAdresse.getKommunenummer(), is(vegadresse.getKommunenummer()));
        assertThat(gateAdresse.getLandkode(), is("NOR"));
        assertThat(gateAdresse.getPostnummer(), is(vegadresse.getPostnummer()));
        assertThat(gateAdresse.getPoststed(), is(vegadresse.getPoststed()));
    }

    private Person createPersonWithBostedsadresseVegadresse() {
        return new Person()
                .withBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null));
    }

    private Person createPersonWithBostedsadresseMatrikkeladresse() {
        return new Person()
                .withBostedsadresse(
                        new Bostedsadresse(
                                "",
                                null,
                                new Matrikkeladresse(
                                        "matrikkelId",
                                        "postnummer",
                                        "poststed",
                                        "tilleggsnavn",
                                        "kommunenummer",
                                        "bruksenhetsnummer"
                                )
                        )
                );
    }

    private Person createPersonWithKontaktadresseVegadresse() {
        return new Person()
                .withBostedsadresse(new Bostedsadresse("", DEFAULT_VEGADRESSE, null))
                .withKontaktadresse(new Kontaktadresse("", ANNEN_VEGADRESSE));
    }
}

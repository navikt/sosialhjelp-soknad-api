package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdresseSystemdataTest {

    private static final String EIER = "12345678901";
    private static final Adresse GATEADRESSE = new Adresse();
    private static final Adresse MATRIKKEL_ADRESSE = new Adresse();
    private static final Adresse.Gateadresse STRUKTURERT_GATEADRESSE = new Adresse.Gateadresse();
    private static final Adresse.MatrikkelAdresse STRUKTURERT_MATRIKKEL_ADRESSE = new Adresse.MatrikkelAdresse();
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
    private PersonaliaFletter personaliaFletter;

    @InjectMocks
    private AdresseSystemdata adresseSystemdata;

    @Test
    public void skalOppdatereFolkeregistrertAdresse() {
        Personalia personalia = new Personalia();
        personalia.setFolkeregistrertAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid);

        JsonAdresse folkeregistrertAdresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getFolkeregistrertAdresse();

        assertThat(folkeregistrertAdresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(folkeregistrertAdresse.getType(), is(JsonAdresse.Type.GATEADRESSE));
        assertThatGateAdresseIsCorrectlyConverted(GATEADRESSE, folkeregistrertAdresse);
    }

    @Test
    public void skalOppdatereOppholdsadresseOgPostAdresseMedFolkeregistrertAdresse() {
        Personalia personalia = new Personalia();
        personalia.setFolkeregistrertAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid);

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
        Personalia personalia = new Personalia();
        personalia.setFolkeregistrertAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse())
                .withPostadresse(new JsonAdresse());
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid);

        JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        JsonAdresse postadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPostadresse();

        assertThat(postadresse.getAdresseValg(), nullValue());
        assertThat(postadresse.getType(), nullValue());
        assertThat(oppholdsadresse.getAdresseValg(), nullValue());
        assertThat(oppholdsadresse.getType(), nullValue());
    }

    @Test
    public void skalOppdatereOppholdsadresseOgPostAdresseMedMidlertidigAdresse() {
        Personalia personalia = new Personalia();
        personalia.setFolkeregistrertAdresse(MATRIKKEL_ADRESSE);
        personalia.setMidlertidigAdresse(GATEADRESSE);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG))
                .withPostadresse(new JsonAdresse().withAdresseValg(JsonAdresseValg.MIDLERTIDIG));
        when(personaliaFletter.mapTilPersonalia(anyString())).thenReturn(personalia);

        adresseSystemdata.updateSystemdataIn(soknadUnderArbeid);

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
}

package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.rest.ressurser.personalia.AdresseRessurs.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.*;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdresseRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final JsonAdresse JSON_SYS_MATRIKKELADRESSE = new JsonMatrikkelAdresse()
            .withKilde(JsonKilde.SYSTEM)
            .withType(JsonAdresse.Type.MATRIKKELADRESSE)
            .withKommunenummer("321")
            .withGaardsnummer("314")
            .withBruksnummer("15")
            .withFestenummer("92")
            .withSeksjonsnummer("65")
            .withUndernummer("36");
    private static final JsonAdresse JSON_SYS_USTRUKTURERT_ADRESSE = new JsonUstrukturertAdresse()
            .withKilde(JsonKilde.SYSTEM)
            .withType(JsonAdresse.Type.USTRUKTURERT).withAdresse(Arrays.asList("Trenger", "Strukturgata", "3"));
    private static final JsonAdresse JSON_BRUKER_GATE_ADRESSE = new JsonGateAdresse()
            .withKilde(JsonKilde.BRUKER)
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode("NOR")
            .withKommunenummer("123")
            .withAdresselinjer(null)
            .withBolignummer("1")
            .withPostnummer("2")
            .withPoststed("Oslo")
            .withGatenavn("Sanntidsgata")
            .withHusnummer("1337")
            .withHusbokstav("A");
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private AdresseSystemdata adresseSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @InjectMocks
    private AdresseRessurs adresseRessurs;

    @Mock
    private NavEnhetRessurs navEnhetRessurs;

    @Mock
    private SubjectHandler subjectHandler;

    @Before
    public void setUp() {
        when(subjectHandler.getUserId()).thenReturn("123");
        when(adresseSystemdata.createDeepCopyOfJsonAdresse(any(JsonAdresse.class))).thenCallRealMethod();
    }

    @Test
    public void getAdresserSkalReturnereAdresserRiktigKonvertert(){
        SoknadUnderArbeid soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_BRUKER_GATE_ADRESSE);
    }

    @Test
    public void getAdresserSkalReturnereOppholdsAdresseLikFolkeregistrertAdresse(){
        SoknadUnderArbeid soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_SYS_MATRIKKELADRESSE);
    }

    @Test
    public void getAdresserSkalReturnereOppholdsAdresseLikMidlertidigAdresse(){
        SoknadUnderArbeid soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.MIDLERTIDIG);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE);
    }

    @Test
    public void getAdresserSkalReturnereAdresserLikNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(null));
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(null);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, null, null, null);
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikFolkeregistrertAdresseOgReturnereTilhorendeNavenhet(){
        SoknadUnderArbeid soknadUnderArbeidIRepo = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD);
        soknadUnderArbeidIRepo.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeidIRepo);
        legacyReturnerNavEnhetTilhorendeValgtAdresse();
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        AdresserFrontend adresserFrontend = new AdresserFrontend()
                .withValg(JsonAdresseValg.FOLKEREGISTRERT);
        final List<NavEnhetRessurs.NavEnhetFrontend> navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse, is(adresseSystemdata.createDeepCopyOfJsonAdresse(JSON_SYS_MATRIKKELADRESSE).withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)));
        assertThat(navEnheter.size(), is(1));
        assertThat(navEnheter.get(0).enhetsnavn, is("Folkeregistrert NavEnhet"));
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikMidlertidigAdresseOgReturnereTilhorendeNavenhet(){
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD));
        legacyReturnerNavEnhetTilhorendeValgtAdresse();
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        AdresserFrontend adresserFrontend = new AdresserFrontend();
        adresserFrontend.withValg(JsonAdresseValg.MIDLERTIDIG);
        final List<NavEnhetRessurs.NavEnhetFrontend> navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse, is(JSON_SYS_USTRUKTURERT_ADRESSE));
        assertThat(oppholdsadresse.getAdresseValg(), is(JsonAdresseValg.MIDLERTIDIG));
        assertThat(navEnheter.size(), is(1));
        assertThat(navEnheter.get(0).enhetsnavn, is("Midlertidig NavEnhet"));
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikSoknadsadresseOgReturnereTilhorendeNavenhet(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT));
        legacyReturnerNavEnhetTilhorendeValgtAdresse();
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        AdresserFrontend adresserFrontend = new AdresserFrontend().withSoknad(new AdresseFrontend());
        adresserFrontend.withValg(JsonAdresseValg.SOKNAD);
        adresserFrontend.soknad.setType(JsonAdresse.Type.GATEADRESSE);
        adresserFrontend.soknad.setGateadresse(new GateadresseFrontend().withGatenavn("Søknadsgata"));
        final List<NavEnhetRessurs.NavEnhetFrontend> navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(((JsonGateAdresse) oppholdsadresse).getGatenavn(), is("Søknadsgata"));
        assertThat(oppholdsadresse.getAdresseValg(), is(JsonAdresseValg.SOKNAD));
        assertThat(navEnheter.size(), is(1));
        assertThat(navEnheter.get(0).enhetsnavn, is("Soknad NavEnhet"));
    }

    private void legacyReturnerNavEnhetTilhorendeValgtAdresse() {
        when(navEnhetRessurs.findSoknadsmottaker(any(JsonSoknad.class), eq("folkeregistrert"), any())).thenReturn(
                Collections.singletonList(new NavEnhetRessurs.NavEnhetFrontend().withEnhetsnavn("Folkeregistrert NavEnhet").withOrgnr("1")));
        when(navEnhetRessurs.findSoknadsmottaker(any(JsonSoknad.class), eq("midlertidig"), any())).thenReturn(
                Collections.singletonList(new NavEnhetRessurs.NavEnhetFrontend().withEnhetsnavn("Midlertidig NavEnhet").withOrgnr("2")));
        when(navEnhetRessurs.findSoknadsmottaker(any(JsonSoknad.class), eq("soknad"), any())).thenReturn(
                Collections.singletonList(new NavEnhetRessurs.NavEnhetFrontend().withEnhetsnavn("Soknad NavEnhet").withOrgnr("3")));
    }

    private void assertThatAdresserAreCorrectlyConverted(AdresserFrontend adresserFrontend, JsonAdresse folkeregAdresse, JsonAdresse midlertidigAdresse, JsonAdresse valgtAdresse){
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.folkeregistrert, folkeregAdresse);
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.midlertidig, midlertidigAdresse);
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.soknad, valgtAdresse);
    }

    private void assertThatAdresseIsCorrectlyConverted(AdresseFrontend adresseFrontend, JsonAdresse jsonAdresse) {
        if (adresseFrontend == null){
            assertThat(jsonAdresse, nullValue());
            return;
        }
        assertThat("Adressetype", adresseFrontend.type, is(jsonAdresse.getType()));
        switch (jsonAdresse.getType()){
            case GATEADRESSE:
                assertThatGateadresseIsCorrectlyConverted(adresseFrontend.gateadresse, jsonAdresse);
                break;
            case MATRIKKELADRESSE:
                assertThatMatrikkeladresseIsCorrectlyConverted(adresseFrontend.matrikkeladresse, jsonAdresse);
                break;
            case USTRUKTURERT:
                assertThatUstrukturertAdresseIsCorrectlyConverted(adresseFrontend.ustrukturert, jsonAdresse);
                break;
            default:
                assertThat(jsonAdresse, nullValue());
                assertThat(adresseFrontend.gateadresse, nullValue());
                assertThat(adresseFrontend.matrikkeladresse, nullValue());
                assertThat(adresseFrontend.ustrukturert, nullValue());
        }
    }

    private void assertThatGateadresseIsCorrectlyConverted(GateadresseFrontend gateadresse, JsonAdresse jsonAdresse) {
        JsonGateAdresse jsonGateAdresse = (JsonGateAdresse) jsonAdresse;
        assertThat("landkode", gateadresse.landkode, is(jsonGateAdresse.getLandkode()));
        assertThat("kommunenummer", gateadresse.kommunenummer, is(jsonGateAdresse.getKommunenummer()));
        assertThat("adresselinjer", gateadresse.adresselinjer, is(jsonGateAdresse.getAdresselinjer()));
        assertThat("bolignummer", gateadresse.bolignummer, is(jsonGateAdresse.getBolignummer()));
        assertThat("postnummer", gateadresse.postnummer, is(jsonGateAdresse.getPostnummer()));
        assertThat("poststed", gateadresse.poststed, is(jsonGateAdresse.getPoststed()));
        assertThat("gatenavn", gateadresse.gatenavn, is(jsonGateAdresse.getGatenavn()));
        assertThat("husnummer", gateadresse.husnummer, is(jsonGateAdresse.getHusnummer()));
        assertThat("husbokstav", gateadresse.husbokstav, is(jsonGateAdresse.getHusbokstav()));
    }

    private void assertThatMatrikkeladresseIsCorrectlyConverted(MatrikkeladresseFrontend matrikkeladresse, JsonAdresse jsonAdresse) {
        JsonMatrikkelAdresse jsonMatrikkelAdresse = (JsonMatrikkelAdresse) jsonAdresse;
        assertThat("kommunenummer", matrikkeladresse.kommunenummer, is(jsonMatrikkelAdresse.getKommunenummer()));
        assertThat("gaardsnummer", matrikkeladresse.gaardsnummer, is(jsonMatrikkelAdresse.getGaardsnummer()));
        assertThat("bruksnummer", matrikkeladresse.bruksnummer, is(jsonMatrikkelAdresse.getBruksnummer()));
        assertThat("festenummer", matrikkeladresse.festenummer, is(jsonMatrikkelAdresse.getFestenummer()));
        assertThat("seksjonsnummer", matrikkeladresse.seksjonsnummer, is(jsonMatrikkelAdresse.getSeksjonsnummer()));
        assertThat("undernummer", matrikkeladresse.undernummer, is(jsonMatrikkelAdresse.getUndernummer()));
    }

    private void assertThatUstrukturertAdresseIsCorrectlyConverted(UstrukturertAdresseFrontend ustrukturertAdresse, JsonAdresse jsonAdresse) {
        JsonUstrukturertAdresse jsonUstrukturertAdresse = (JsonUstrukturertAdresse) jsonAdresse;
        assertThat("adresse", ustrukturertAdresse.adresse, is(jsonUstrukturertAdresse.getAdresse()));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithOppholdsadresse(final JsonAdresseValg valg) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia()
                .withOppholdsadresse(getSelectedAdresse(valg));
        return soknadUnderArbeid;
    }

    private JsonAdresse getSelectedAdresse(JsonAdresseValg valg) {
        if (valg == null){
            return null;
        }
        switch (valg) {
            case FOLKEREGISTRERT:
                return JSON_SYS_MATRIKKELADRESSE;
            case MIDLERTIDIG:
                return JSON_SYS_USTRUKTURERT_ADRESSE;
            case SOKNAD:
                return JSON_BRUKER_GATE_ADRESSE;
            default:
                return null;
        }
    }
}
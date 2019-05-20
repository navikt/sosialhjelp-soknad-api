package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadsmottakerRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.personalia.AdresseRessurs.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.*;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
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
    private LegacyHelper legacyHelper;

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private AdresseSystemdata adresseSystemdata;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SoknadService soknadService;

    @Mock
    private FaktaService faktaService;

    @Mock
    private SoknadsmottakerRessurs soknadsmottakerRessurs;

    @InjectMocks
    private AdresseRessurs adresseRessurs;

    @Mock
    private NavEnhetRessurs navEnhetRessurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
        when(navEnhetRessurs.mapFromLegacyNavEnhetFrontend(any(SoknadsmottakerRessurs.LegacyNavEnhetFrontend.class), anyString())).thenCallRealMethod();
        when(adresseSystemdata.createDeepCopyOfJsonAdresse(any(JsonAdresse.class))).thenCallRealMethod();
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getAdresserSkalReturnereAdresserRiktigKonvertert(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD));
        when(adresseSystemdata.innhentFolkeregistrertAdresse(anyString())).thenReturn(JSON_SYS_MATRIKKELADRESSE);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_BRUKER_GATE_ADRESSE);
    }

    @Test
    public void getAdresserSkalReturnereOppholdsAdresseLikFolkeregistrertAdresse(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT));
        when(adresseSystemdata.innhentFolkeregistrertAdresse(anyString())).thenReturn(JSON_SYS_MATRIKKELADRESSE);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_SYS_MATRIKKELADRESSE);
    }

    @Test
    public void getAdresserSkalReturnereOppholdsAdresseLikMidlertidigAdresse(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.MIDLERTIDIG));
        when(adresseSystemdata.innhentFolkeregistrertAdresse(anyString())).thenReturn(JSON_SYS_MATRIKKELADRESSE);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE);
    }

    @Test
    public void getAdresserSkalReturnereAdresserLikNull(){
        when(legacyHelper.hentSoknad(anyString(), anyString(), anyBoolean())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(null));
        when(adresseSystemdata.innhentFolkeregistrertAdresse(anyString())).thenReturn(null);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(null);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, null, null, null);
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikFolkeregistrertAdresseOgReturnereTilhorendeNavenhet(){
        when(adresseSystemdata.innhentFolkeregistrertAdresse(anyString())).thenReturn(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD)));
        legacyReturnerNavEnhetTilhorendeValgtAdresse();
        ignoreTilgangskontrollAndLegacyUpdate();

        AdresserFrontend adresserFrontend = new AdresserFrontend();
        adresserFrontend.withValg(JsonAdresseValg.FOLKEREGISTRERT);
        final List<NavEnhetRessurs.NavEnhetFrontend> navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        assertThat(oppholdsadresse.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(oppholdsadresse, is(JSON_SYS_MATRIKKELADRESSE));
        assertThat(oppholdsadresse.getAdresseValg(), is(JsonAdresseValg.FOLKEREGISTRERT));
        assertThat(navEnheter.size(), is(1));
        assertThat(navEnheter.get(0).enhetsnavn, is("Folkeregistrert NavEnhet"));
    }

    @Test
    public void putAdresseSkalSetteOppholdsAdresseLikMidlertidigAdresseOgReturnereTilhorendeNavenhet(){
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                Optional.of(createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD)));
        legacyReturnerNavEnhetTilhorendeValgtAdresse();
        ignoreTilgangskontrollAndLegacyUpdate();

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
                Optional.of(createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT)));
        legacyReturnerNavEnhetTilhorendeValgtAdresse();
        ignoreTilgangskontrollAndLegacyUpdate();

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
        when(soknadsmottakerRessurs.findSoknadsmottaker(BEHANDLINGSID, "folkeregistrert")).thenReturn(
                Collections.singletonList(new SoknadsmottakerRessurs.LegacyNavEnhetFrontend().withEnhetsnavn("Folkeregistrert NavEnhet").withSosialOrgnr("1")));
        when(soknadsmottakerRessurs.findSoknadsmottaker(BEHANDLINGSID, "midlertidig")).thenReturn(
                Collections.singletonList(new SoknadsmottakerRessurs.LegacyNavEnhetFrontend().withEnhetsnavn("Midlertidig NavEnhet").withSosialOrgnr("2")));
        when(soknadsmottakerRessurs.findSoknadsmottaker(BEHANDLINGSID, "soknad")).thenReturn(
                Collections.singletonList(new SoknadsmottakerRessurs.LegacyNavEnhetFrontend().withEnhetsnavn("Soknad NavEnhet").withSosialOrgnr("3")));
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

    private void ignoreTilgangskontrollAndLegacyUpdate() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadService.hentSoknad(anyString(), anyBoolean(), anyBoolean())).thenReturn(new WebSoknad());
        when(faktaService.hentFaktumMedKey(anyLong(), anyString())).thenReturn(new Faktum());
        when(faktaService.lagreBrukerFaktum(any(Faktum.class))).thenReturn(new Faktum());
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
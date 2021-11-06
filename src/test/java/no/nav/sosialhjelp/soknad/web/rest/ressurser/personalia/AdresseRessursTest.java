package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.systemdata.AdresseSystemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs;
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.AdresseRessurs.AdresseFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.AdresseRessurs.AdresserFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.AdresseRessurs.GateadresseFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.AdresseRessurs.MatrikkeladresseFrontend;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia.AdresseRessurs.UstrukturertAdresseFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdresseRessursTest {

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

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void getAdresserSkalReturnereAdresserRiktigKonvertert() {
        SoknadUnderArbeid soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_BRUKER_GATE_ADRESSE);
    }

    @Test
    void getAdresserSkalReturnereOppholdsAdresseLikFolkeregistrertAdresse() {
        SoknadUnderArbeid soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_SYS_MATRIKKELADRESSE);
    }

    @Test
    void getAdresserSkalReturnereOppholdsAdresseLikMidlertidigAdresse() {
        SoknadUnderArbeid soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.MIDLERTIDIG);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, JSON_SYS_MATRIKKELADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE, JSON_SYS_USTRUKTURERT_ADRESSE);
    }

    @Test
    void getAdresserSkalReturnereAdresserLikNull() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(null));
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(null);

        final AdresserFrontend adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID);

        assertThatAdresserAreCorrectlyConverted(adresserFrontend, null, null, null);
    }

    @Test
    void putAdresseSkalSetteOppholdsAdresseLikFolkeregistrertAdresseOgReturnereTilhorendeNavenhet() {
        SoknadUnderArbeid soknadUnderArbeidIRepo = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD);
        soknadUnderArbeidIRepo.getJsonInternalSoknad().getSoknad().getData().getPersonalia().setFolkeregistrertAdresse(JSON_SYS_MATRIKKELADRESSE);
        when(adresseSystemdata.createDeepCopyOfJsonAdresse(any(JsonAdresse.class))).thenCallRealMethod();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeidIRepo);
        when(navEnhetRessurs.findSoknadsmottaker(anyString(), any(JsonSoknad.class), eq("folkeregistrert"), any()))
                .thenReturn(singletonList(
                        new NavEnhetFrontend("1", "1111", "Folkeregistrert NavEnhet", "4321", null, null, null, null, null)
                ));
//                        .withEnhetsnavn("Folkeregistrert NavEnhet").withOrgnr("1")));
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        AdresserFrontend adresserFrontend = new AdresserFrontend()
                .withValg(JsonAdresseValg.FOLKEREGISTRERT);
        final List<NavEnhetFrontend> navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        assertThat(oppholdsadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(oppholdsadresse).isEqualTo(adresseSystemdata.createDeepCopyOfJsonAdresse(JSON_SYS_MATRIKKELADRESSE).withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT));
        assertThat(navEnheter).hasSize(1);
        assertThat(navEnheter.get(0).getEnhetsnavn()).isEqualTo("Folkeregistrert NavEnhet");
    }

    @Test
    void putAdresseSkalSetteOppholdsAdresseLikMidlertidigAdresseOgReturnereTilhorendeNavenhet() {
        when(adresseSystemdata.innhentMidlertidigAdresse(anyString())).thenReturn(JSON_SYS_USTRUKTURERT_ADRESSE);
        when(adresseSystemdata.createDeepCopyOfJsonAdresse(any(JsonAdresse.class))).thenCallRealMethod();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD));
        when(navEnhetRessurs.findSoknadsmottaker(anyString(), any(JsonSoknad.class), eq("midlertidig"), any()))
                .thenReturn(singletonList(
//                        new NavEnhetRessurs.NavEnhetFrontend().withEnhetsnavn("Midlertidig NavEnhet").withOrgnr("2")
                        new NavEnhetFrontend("2", "2222", "Midlertidig NavEnhet", "kommune", "4321", null, null, null, null)
                ));
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        AdresserFrontend adresserFrontend = new AdresserFrontend();
        adresserFrontend.withValg(JsonAdresseValg.MIDLERTIDIG);
        final List<NavEnhetFrontend> navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        assertThat(oppholdsadresse.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(oppholdsadresse).isEqualTo(JSON_SYS_USTRUKTURERT_ADRESSE);
        assertThat(oppholdsadresse.getAdresseValg()).isEqualTo(JsonAdresseValg.MIDLERTIDIG);
        assertThat(navEnheter).hasSize(1);
        assertThat(navEnheter.get(0).getEnhetsnavn()).isEqualTo("Midlertidig NavEnhet");
    }

    @Test
    void putAdresseSkalSetteOppholdsAdresseLikSoknadsadresseOgReturnereTilhorendeNavenhet() {
        when(adresseSystemdata.createDeepCopyOfJsonAdresse(any(JsonAdresse.class))).thenCallRealMethod();
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT));
        when(navEnhetRessurs.findSoknadsmottaker(anyString(), any(JsonSoknad.class), eq("soknad"), any()))
                .thenReturn(singletonList(
//                        new NavEnhetRessurs.NavEnhetFrontend().withEnhetsnavn("Soknad NavEnhet").withOrgnr("3")
                        new NavEnhetFrontend("3", "333", "Soknad NavEnhet", "4321", null, null, null, null, null)
                ));
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        AdresserFrontend adresserFrontend = new AdresserFrontend().withSoknad(new AdresseFrontend());
        adresserFrontend.withValg(JsonAdresseValg.SOKNAD);
        adresserFrontend.soknad.setType(JsonAdresse.Type.GATEADRESSE);
        adresserFrontend.soknad.setGateadresse(new GateadresseFrontend().withGatenavn("Søknadsgata"));
        final List<NavEnhetFrontend> navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend);

        final SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        final JsonAdresse oppholdsadresse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getOppholdsadresse();
        assertThat(oppholdsadresse.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(((JsonGateAdresse) oppholdsadresse).getGatenavn()).isEqualTo("Søknadsgata");
        assertThat(oppholdsadresse.getAdresseValg()).isEqualTo(JsonAdresseValg.SOKNAD);
        assertThat(navEnheter).hasSize(1);
        assertThat(navEnheter.get(0).getEnhetsnavn()).isEqualTo("Soknad NavEnhet");
    }

    @Test
    void getAdresserSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> adresseRessurs.hentAdresser(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void putAdresserSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        var adresserFrontend = new AdresserFrontend();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private void assertThatAdresserAreCorrectlyConverted(AdresserFrontend adresserFrontend, JsonAdresse folkeregAdresse, JsonAdresse midlertidigAdresse, JsonAdresse valgtAdresse) {
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.folkeregistrert, folkeregAdresse);
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.midlertidig, midlertidigAdresse);
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.soknad, valgtAdresse);
    }

    private void assertThatAdresseIsCorrectlyConverted(AdresseFrontend adresseFrontend, JsonAdresse jsonAdresse) {
        if (adresseFrontend == null) {
            assertThat(jsonAdresse).isNull();
            return;
        }
        assertThat(adresseFrontend.type).isEqualTo(jsonAdresse.getType());
        switch (jsonAdresse.getType()) {
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
                assertThat(jsonAdresse).isNull();
                assertThat(adresseFrontend.gateadresse).isNull();
                assertThat(adresseFrontend.matrikkeladresse).isNull();
                assertThat(adresseFrontend.ustrukturert).isNull();
        }
    }

    private void assertThatGateadresseIsCorrectlyConverted(GateadresseFrontend gateadresse, JsonAdresse jsonAdresse) {
        JsonGateAdresse jsonGateAdresse = (JsonGateAdresse) jsonAdresse;
        assertThat(gateadresse.landkode).isEqualTo(jsonGateAdresse.getLandkode());
        assertThat(gateadresse.kommunenummer).isEqualTo(jsonGateAdresse.getKommunenummer());
        assertThat(gateadresse.adresselinjer).isEqualTo(jsonGateAdresse.getAdresselinjer());
        assertThat(gateadresse.bolignummer).isEqualTo(jsonGateAdresse.getBolignummer());
        assertThat(gateadresse.postnummer).isEqualTo(jsonGateAdresse.getPostnummer());
        assertThat(gateadresse.poststed).isEqualTo(jsonGateAdresse.getPoststed());
        assertThat(gateadresse.gatenavn).isEqualTo(jsonGateAdresse.getGatenavn());
        assertThat(gateadresse.husnummer).isEqualTo(jsonGateAdresse.getHusnummer());
        assertThat(gateadresse.husbokstav).isEqualTo(jsonGateAdresse.getHusbokstav());
    }

    private void assertThatMatrikkeladresseIsCorrectlyConverted(MatrikkeladresseFrontend matrikkeladresse, JsonAdresse jsonAdresse) {
        JsonMatrikkelAdresse jsonMatrikkelAdresse = (JsonMatrikkelAdresse) jsonAdresse;
        assertThat(matrikkeladresse.kommunenummer).isEqualTo(jsonMatrikkelAdresse.getKommunenummer());
        assertThat(matrikkeladresse.gaardsnummer).isEqualTo(jsonMatrikkelAdresse.getGaardsnummer());
        assertThat(matrikkeladresse.bruksnummer).isEqualTo(jsonMatrikkelAdresse.getBruksnummer());
        assertThat(matrikkeladresse.festenummer).isEqualTo(jsonMatrikkelAdresse.getFestenummer());
        assertThat(matrikkeladresse.seksjonsnummer).isEqualTo(jsonMatrikkelAdresse.getSeksjonsnummer());
        assertThat(matrikkeladresse.undernummer).isEqualTo(jsonMatrikkelAdresse.getUndernummer());
    }

    private void assertThatUstrukturertAdresseIsCorrectlyConverted(UstrukturertAdresseFrontend ustrukturertAdresse, JsonAdresse jsonAdresse) {
        JsonUstrukturertAdresse jsonUstrukturertAdresse = (JsonUstrukturertAdresse) jsonAdresse;
        assertThat(ustrukturertAdresse.adresse).isEqualTo(jsonUstrukturertAdresse.getAdresse());
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
        if (valg == null) {
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
package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.sbl.dialogarena.rest.ressurser.inntekt.VerdiRessurs.VerdierFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
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

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VerdiRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String BEKREFTELSE_TYPE = "verdi";
    private static final String BOLIG_TYPE = "bolig";
    private static final String CAMPINGVOGN_TYPE = "campingvogn";
    private static final String KJORETOY_TYPE = "kjoretoy";
    private static final String FRITIDSEIENDOM_TYPE = "fritidseiendom";
    private static final String ANNET_TYPE = "annet";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private VerdiRessurs verdiRessurs;

    @Before
    public void setUp() {
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
    }

    @After
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void getVerdierSkalReturnereBekreftelseLikNullOgAltFalse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        VerdierFrontend verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID);

        assertThat(verdierFrontend.bekreftelse, nullValue());
        assertFalse(verdierFrontend.bolig);
        assertFalse(verdierFrontend.campingvogn);
        assertFalse(verdierFrontend.kjoretoy);
        assertFalse(verdierFrontend.fritidseiendom);
        assertFalse(verdierFrontend.annet);
        assertThat(verdierFrontend.beskrivelseAvAnnet, nullValue());
    }

    @Test
    public void getVerdierSkalReturnereBekreftelserLikTrue(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithVerdier(true, asList(BOLIG_TYPE, CAMPINGVOGN_TYPE, KJORETOY_TYPE,
                        FRITIDSEIENDOM_TYPE, ANNET_TYPE), null));

        VerdierFrontend verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID);

        assertTrue(verdierFrontend.bekreftelse);
        assertTrue(verdierFrontend.bolig);
        assertTrue(verdierFrontend.campingvogn);
        assertTrue(verdierFrontend.kjoretoy);
        assertTrue(verdierFrontend.fritidseiendom);
        assertTrue(verdierFrontend.annet);
        assertThat(verdierFrontend.beskrivelseAvAnnet, nullValue());
    }

    @Test
    public void getVerdierSkalReturnereBeskrivelseAvAnnet(){
        String beskrivelse = "Bestefars klokke";
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithVerdier(true, asList(ANNET_TYPE), beskrivelse));

        VerdierFrontend verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID);

        assertTrue(verdierFrontend.bekreftelse);
        assertTrue(verdierFrontend.annet);
        assertThat(verdierFrontend.beskrivelseAvAnnet, is(beskrivelse));
    }

    @Test
    public void putVerdierSkalSetteAltFalseDersomManVelgerHarIkkeVerdier(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithVerdier(true, asList(BOLIG_TYPE, CAMPINGVOGN_TYPE, KJORETOY_TYPE,
                        ANNET_TYPE), "Bestefars klokke"));

        VerdierFrontend verdierFrontend = new VerdierFrontend();
        verdierFrontend.setBekreftelse(false);
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getFormue();
        assertFalse(verdiBekreftelse.getVerdi());
        assertTrue(verdier.isEmpty());
    }

    @Test
    public void putVerdierSkalSetteAlleBekreftelserLikFalse(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithVerdier(true, asList(BOLIG_TYPE, CAMPINGVOGN_TYPE,
                        KJORETOY_TYPE, ANNET_TYPE), "Bestefars klokke"));

        VerdierFrontend verdierFrontend = new VerdierFrontend();
        verdierFrontend.setBekreftelse(false);
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getFormue();
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getVerdi();
        assertFalse(verdiBekreftelse.getVerdi());
        assertTrue(verdier.isEmpty());
        assertThat(beskrivelse, is(""));
    }

    @Test
    public void putVerdierSkalSetteNoenBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        VerdierFrontend verdierFrontend = new VerdierFrontend();
        verdierFrontend.setBekreftelse(true);
        verdierFrontend.setBolig(true);
        verdierFrontend.setCampingvogn(true);
        verdierFrontend.setFritidseiendom(false);
        verdierFrontend.setKjoretoy(false);
        verdierFrontend.setAnnet(false);
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getFormue();
        assertThat(verdiBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(verdiBekreftelse.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(verdiBekreftelse.getVerdi());
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(BOLIG_TYPE)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(CAMPINGVOGN_TYPE)));
        assertFalse(verdier.stream().anyMatch(verdi -> verdi.getType().equals(KJORETOY_TYPE)));
        assertFalse(verdier.stream().anyMatch(verdi -> verdi.getType().equals(FRITIDSEIENDOM_TYPE)));
        assertFalse(verdier.stream().anyMatch(verdi -> verdi.getType().equals(ANNET_TYPE)));
    }

    @Test
    public void putVerdierSkalSetteAlleBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        VerdierFrontend verdierFrontend = new VerdierFrontend();
        verdierFrontend.setBekreftelse(true);
        verdierFrontend.setBolig(true);
        verdierFrontend.setCampingvogn(true);
        verdierFrontend.setFritidseiendom(true);
        verdierFrontend.setKjoretoy(true);
        verdierFrontend.setAnnet(true);
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktFormue> verdier = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getFormue();
        assertThat(verdiBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(verdiBekreftelse.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(verdiBekreftelse.getVerdi());
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(KJORETOY_TYPE)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(CAMPINGVOGN_TYPE)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(BOLIG_TYPE)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(FRITIDSEIENDOM_TYPE)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(ANNET_TYPE)));
    }

    @Test
    public void putVerdierSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithVerdier(true, asList(ANNET_TYPE), "Vinylplater"));

        VerdierFrontend verdierFrontend = new VerdierFrontend();
        verdierFrontend.setBekreftelse(false);
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse verdiBekreftelse = bekreftelser.get(0);
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getVerdi();
        assertFalse(verdiBekreftelse.getVerdi());
        assertThat(beskrivelse, is(""));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithVerdier(Boolean harVerdier, List<String> verdiTyper, String beskrivelseAvAnnet) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomioversiktFormue> verdier = new ArrayList<>();
        for (String verdi: verdiTyper) {
            verdier.add(new JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(verdi)
                    .withTittel("tittel"));
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(asList(new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(BEKREFTELSE_TYPE)
                .withVerdi(harVerdier)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setFormue(verdier);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBeskrivelseAvAnnet(
                new JsonOkonomibeskrivelserAvAnnet().withVerdi(beskrivelseAvAnnet));
        return soknadUnderArbeid;
    }
}

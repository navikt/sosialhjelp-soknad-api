package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.sbl.dialogarena.rest.ressurser.inntekt.BostotteRessurs.BostotteFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
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
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BostotteRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private BostotteRessurs bostotteRessurs;

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
    public void getBostotteSkalReturnereNull(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotteBekreftelse(BEHANDLINGSID);

        assertThat(bostotteFrontend.bekreftelse, nullValue());
    }

    @Test
    public void getBostotteSkalReturnereBekreftetBostotte(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBostotte(true));

        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotteBekreftelse(BEHANDLINGSID);

        assertTrue(bostotteFrontend.bekreftelse);
    }

    @Test
    public void getBostotteSkalReturnereHarIkkeBostotte(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBostotte(false));

        BostotteFrontend bostotteFrontend = bostotteRessurs.hentBostotteBekreftelse(BEHANDLINGSID);

        assertFalse(bostotteFrontend.bekreftelse);
    }

    @Test
    public void putBostotteSkalSetteBostotteOgLeggeTilInntektstypen(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BostotteRessurs.BostotteFrontend bostotteFrontend = new BostotteRessurs.BostotteFrontend();
        bostotteFrontend.setBekreftelse(true);
        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        List<JsonOkonomioversiktInntekt> inntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getInntekt();
        assertEquals(inntekt.get(0).getType(), BOSTOTTE);
        JsonOkonomibekreftelse bostotte = bekreftelser.get(0);
        assertThat(bostotte.getKilde(), is(JsonKilde.BRUKER));
        assertThat(bostotte.getType(), is(BOSTOTTE));
        assertTrue(bostotte.getVerdi());
    }

    @Test
    public void putBostotteSkalSetteHarIkkeBostotteOgSletteInntektstypen(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        SoknadUnderArbeid soknad = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        ArrayList<JsonOkonomioversiktInntekt> inntekt = new ArrayList<>();
        inntekt.add(new JsonOkonomioversiktInntekt().withType(BOSTOTTE));
        soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setInntekt(inntekt);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);

        BostotteRessurs.BostotteFrontend bostotteFrontend = new BostotteRessurs.BostotteFrontend();
        bostotteFrontend.setBekreftelse(false);
        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        List<JsonOkonomioversiktInntekt> jsonInntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getInntekt();
        assertTrue(jsonInntekt.isEmpty());
        JsonOkonomibekreftelse bostotte = bekreftelser.get(0);
        assertThat(bostotte.getKilde(), is(JsonKilde.BRUKER));
        assertThat(bostotte.getType(), is(BOSTOTTE));
        assertFalse(bostotte.getVerdi());
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBostotte(Boolean verdi) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().withBekreftelse(
                asList(new JsonOkonomibekreftelse()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(BOSTOTTE)
                        .withVerdi(verdi)));
        return soknadUnderArbeid;
    }
}

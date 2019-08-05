package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.sbl.dialogarena.rest.ressurser.inntekt.FormueRessurs.FormueFrontend;
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
public class FormueRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String BEKREFTELSE_TYPE = "sparing";
    private static final String BRUKSKONTO_TYPE = "brukskonto";
    private static final String BSU_TYPE = "bsu";
    private static final String LIVSFORSIKRING_TYPE = "livsforsikringssparedel";
    private static final String VERDIPAPIRER_TYPE = "verdipapirer";
    private static final String SPAREKONTO_TYPE = "sparekonto";
    private static final String ANNET_TYPE = "belop";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private FormueRessurs formueRessurs;

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
    public void getFormueSkalReturnereBekreftelserLikFalse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        FormueFrontend formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID);

        assertFalse(formueFrontend.brukskonto);
        assertFalse(formueFrontend.bsu);
        assertFalse(formueFrontend.livsforsikring);
        assertFalse(formueFrontend.sparekonto);
        assertFalse(formueFrontend.verdipapirer);
        assertFalse(formueFrontend.annet);
        assertThat(formueFrontend.beskrivelseAvAnnet, nullValue());
    }

    @Test
    public void getFormueSkalReturnereBekreftelserLikTrue(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithFormue(asList(BRUKSKONTO_TYPE, BSU_TYPE,
                        LIVSFORSIKRING_TYPE, VERDIPAPIRER_TYPE, SPAREKONTO_TYPE, ANNET_TYPE), null));

        FormueFrontend formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID);

        assertTrue(formueFrontend.brukskonto);
        assertTrue(formueFrontend.bsu);
        assertTrue(formueFrontend.livsforsikring);
        assertTrue(formueFrontend.sparekonto);
        assertTrue(formueFrontend.verdipapirer);
        assertTrue(formueFrontend.annet);
        assertThat(formueFrontend.beskrivelseAvAnnet, nullValue());
    }

    @Test
    public void getFormueSkalReturnereBeskrivelseAvAnnet(){
        String beskrivelse = "Vinylplater";
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithFormue(asList(ANNET_TYPE), beskrivelse));

        FormueFrontend formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID);

        assertTrue(formueFrontend.annet);
        assertThat(formueFrontend.beskrivelseAvAnnet, is(beskrivelse));
    }

    @Test
    public void putFormueSkalSetteAlleBekreftelserLikFalse(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithFormue(asList(BRUKSKONTO_TYPE, BSU_TYPE,
                        LIVSFORSIKRING_TYPE, VERDIPAPIRER_TYPE, SPAREKONTO_TYPE, ANNET_TYPE), "Vinylplater"));

        FormueFrontend formueFrontend = new FormueFrontend();
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
        List<JsonOkonomioversiktFormue> formuer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getFormue();
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getSparing();
        assertFalse(sparing.getVerdi());
        assertTrue(formuer.isEmpty());
        assertThat(beskrivelse, is(""));
    }
    
    @Test
    public void putFormueSkalSetteNoenBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        FormueFrontend formueFrontend = new FormueFrontend();
        formueFrontend.setBrukskonto(true);
        formueFrontend.setBsu(true);
        formueFrontend.setLivsforsikring(true);
        formueFrontend.setSparekonto(false);
        formueFrontend.setVerdipapirer(false);
        formueFrontend.setAnnet(false);
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
        List<JsonOkonomioversiktFormue> formuer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getFormue();
        assertThat(sparing.getKilde(), is(JsonKilde.BRUKER));
        assertThat(sparing.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(sparing.getVerdi());
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(BRUKSKONTO_TYPE)));
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(BSU_TYPE)));
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(LIVSFORSIKRING_TYPE)));
        assertFalse(formuer.stream().anyMatch(formue -> formue.getType().equals(SPAREKONTO_TYPE)));
        assertFalse(formuer.stream().anyMatch(formue -> formue.getType().equals(VERDIPAPIRER_TYPE)));
        assertFalse(formuer.stream().anyMatch(formue -> formue.getType().equals(ANNET_TYPE)));
    }

    @Test
    public void putFormueSkalSetteAlleBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        FormueFrontend formueFrontend = new FormueFrontend();
        formueFrontend.setBrukskonto(true);
        formueFrontend.setBsu(true);
        formueFrontend.setLivsforsikring(true);
        formueFrontend.setSparekonto(true);
        formueFrontend.setVerdipapirer(true);
        formueFrontend.setAnnet(true);
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
        List<JsonOkonomioversiktFormue> formuer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getFormue();
        assertThat(sparing.getKilde(), is(JsonKilde.BRUKER));
        assertThat(sparing.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(sparing.getVerdi());
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(BRUKSKONTO_TYPE)));
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(BSU_TYPE)));
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(LIVSFORSIKRING_TYPE)));
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(SPAREKONTO_TYPE)));
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(VERDIPAPIRER_TYPE)));
        assertTrue(formuer.stream().anyMatch(formue -> formue.getType().equals(ANNET_TYPE)));
    }

    @Test
    public void putFormueSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithFormue(asList(ANNET_TYPE), "Vinylplater"));

        FormueFrontend formueFrontend = new FormueFrontend();
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse sparing = bekreftelser.get(0);
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getSparing();
        assertFalse(sparing.getVerdi());
        assertThat(beskrivelse, is(""));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithFormue(List<String> formueTyper, String beskrivelseAvAnnet) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomioversiktFormue> formuer = new ArrayList<>();
        for (String formue: formueTyper) {
            formuer.add(new JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(formue)
                    .withTittel("tittel"));
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setFormue(formuer);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBeskrivelseAvAnnet(
                new JsonOkonomibeskrivelserAvAnnet().withSparing(beskrivelseAvAnnet));
        return soknadUnderArbeid;
    }
}

package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.sbl.dialogarena.rest.ressurser.inntekt.VerdiRessurs.VerdierFrontend;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VerdiRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @Mock
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @InjectMocks
    private VerdiRessurs verdiRessurs;

    @Before
    public void setUp() {
        when(subjectHandlerWrapper.getIdent()).thenReturn("123");
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
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
                createJsonInternalSoknadWithVerdier(true, asList(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY,
                        VERDI_FRITIDSEIENDOM, VERDI_ANNET), null));

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
                createJsonInternalSoknadWithVerdier(true, asList(VERDI_ANNET), beskrivelse));

        VerdierFrontend verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID);

        assertTrue(verdierFrontend.bekreftelse);
        assertTrue(verdierFrontend.annet);
        assertThat(verdierFrontend.beskrivelseAvAnnet, is(beskrivelse));
    }

    @Test
    public void putVerdierSkalSetteAltFalseDersomManVelgerHarIkkeVerdier(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithVerdier(true, asList(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY,
                        VERDI_ANNET), "Bestefars klokke"));

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
                createJsonInternalSoknadWithVerdier(true, asList(VERDI_BOLIG, VERDI_CAMPINGVOGN,
                        VERDI_KJORETOY, VERDI_ANNET), "Bestefars klokke"));

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
        assertThat(verdiBekreftelse.getType(), is(BEKREFTELSE_VERDI));
        assertTrue(verdiBekreftelse.getVerdi());
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_BOLIG)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_CAMPINGVOGN)));
        assertFalse(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_KJORETOY)));
        assertFalse(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_FRITIDSEIENDOM)));
        assertFalse(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_ANNET)));
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
        assertThat(verdiBekreftelse.getType(), is(BEKREFTELSE_VERDI));
        assertTrue(verdiBekreftelse.getVerdi());
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_KJORETOY)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_CAMPINGVOGN)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_BOLIG)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_FRITIDSEIENDOM)));
        assertTrue(verdier.stream().anyMatch(verdi -> verdi.getType().equals(VERDI_ANNET)));
    }

    @Test
    public void putVerdierSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithVerdier(true, asList(VERDI_ANNET), "Vinylplater"));

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
                .withType(BEKREFTELSE_VERDI)
                .withVerdi(harVerdier)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setFormue(verdier);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBeskrivelseAvAnnet(
                new JsonOkonomibeskrivelserAvAnnet().withVerdi(beskrivelseAvAnnet));
        return soknadUnderArbeid;
    }
}

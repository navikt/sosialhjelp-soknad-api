package no.nav.sbl.dialogarena.rest.ressurser.utgifter;

import no.nav.sbl.dialogarena.rest.ressurser.utgifter.BarneutgiftRessurs.BarneutgifterFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
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
import java.util.Collections;
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
public class BarneutgiftRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String BEKREFTELSE_TYPE = "barneutgifter";
    private static final String BARNEHAGE_TYPE = "barnehage";
    private static final String SFO_TYPE = "sfo";
    private static final String FRITIDSAKTIVITETER_TYPE = "barnFritidsaktiviteter";
    private static final String TANNREGULERING_TYPE = "barnTannregulering";
    private static final String ANNET_TYPE = "annenBarneutgift";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private BarneutgiftRessurs barneutgiftRessurs;

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
    public void getBarneutgifterSkalReturnereBekreftelseLikNullOgAltFalse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BarneutgifterFrontend barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID);

        assertFalse(barneutgifterFrontend.harForsorgerplikt);
        assertThat(barneutgifterFrontend.bekreftelse, nullValue());
        assertFalse(barneutgifterFrontend.barnehage);
        assertFalse(barneutgifterFrontend.sfo);
        assertFalse(barneutgifterFrontend.tannregulering);
        assertFalse(barneutgifterFrontend.fritidsaktiviteter);
        assertFalse(barneutgifterFrontend.annet);
    }

    @Test
    public void getBarneutgifterSkalReturnereHarForsorgerpliktLikFalseForPersonUtenBarn(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBarneutgifter(false, false, Collections.emptyList()));

        BarneutgifterFrontend barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID);

        assertFalse(barneutgifterFrontend.harForsorgerplikt);
        assertThat(barneutgifterFrontend.bekreftelse, nullValue());
        assertFalse(barneutgifterFrontend.barnehage);
        assertFalse(barneutgifterFrontend.sfo);
        assertFalse(barneutgifterFrontend.tannregulering);
        assertFalse(barneutgifterFrontend.fritidsaktiviteter);
        assertFalse(barneutgifterFrontend.annet);
    }

    @Test
    public void getBarneutgifterSkalReturnereBekreftelserLikTrue(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBarneutgifter(true, true, asList(BARNEHAGE_TYPE, SFO_TYPE, FRITIDSAKTIVITETER_TYPE,
                        TANNREGULERING_TYPE, ANNET_TYPE)));

        BarneutgifterFrontend barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID);

        assertTrue(barneutgifterFrontend.harForsorgerplikt);
        assertTrue(barneutgifterFrontend.bekreftelse);
        assertTrue(barneutgifterFrontend.barnehage);
        assertTrue(barneutgifterFrontend.sfo);
        assertTrue(barneutgifterFrontend.tannregulering);
        assertTrue(barneutgifterFrontend.fritidsaktiviteter);
        assertTrue(barneutgifterFrontend.annet);
    }

    @Test
    public void putBarneutgifterSkalSetteAltFalseDersomManVelgerHarIkkeBarneutgifter(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBarneutgifter(true, true, asList(BARNEHAGE_TYPE, SFO_TYPE,
                        FRITIDSAKTIVITETER_TYPE, ANNET_TYPE)));

        BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend();
        barneutgifterFrontend.setBekreftelse(false);
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse barneutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertFalse(barneutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBarneutgifter.isEmpty());
        assertTrue(opplysningerBarneutgifter.isEmpty());
    }

    @Test
    public void putBarneutgifterSkalSetteNoenBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend();
        barneutgifterFrontend.setBekreftelse(true);
        barneutgifterFrontend.setBarnehage(true);
        barneutgifterFrontend.setSfo(true);
        barneutgifterFrontend.setTannregulering(false);
        barneutgifterFrontend.setFritidsaktiviteter(false);
        barneutgifterFrontend.setAnnet(false);
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse barneutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertThat(barneutgiftBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(barneutgiftBekreftelse.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(barneutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(BARNEHAGE_TYPE)));
        assertTrue(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(SFO_TYPE)));
        assertFalse(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(FRITIDSAKTIVITETER_TYPE)));
        assertFalse(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(TANNREGULERING_TYPE)));
        assertFalse(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(ANNET_TYPE)));
    }

    @Test
    public void putBarneutgifterSkalSetteAlleBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BarneutgifterFrontend barneutgifterFrontend = new BarneutgifterFrontend();
        barneutgifterFrontend.setBekreftelse(true);
        barneutgifterFrontend.setBarnehage(true);
        barneutgifterFrontend.setSfo(true);
        barneutgifterFrontend.setTannregulering(true);
        barneutgifterFrontend.setFritidsaktiviteter(true);
        barneutgifterFrontend.setAnnet(true);
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse barneutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertThat(barneutgiftBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(barneutgiftBekreftelse.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(barneutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(BARNEHAGE_TYPE)));
        assertTrue(oversiktBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(SFO_TYPE)));
        assertTrue(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(FRITIDSAKTIVITETER_TYPE)));
        assertTrue(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(TANNREGULERING_TYPE)));
        assertTrue(opplysningerBarneutgifter.stream().anyMatch(barneutgift -> barneutgift.getType().equals(ANNET_TYPE)));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBarneutgifter(Boolean harForsorgerplikt, Boolean harUtgifter, List<String> utgiftstyper) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomioversiktUtgift> oversiktUtgifter = new ArrayList<>();
        List<JsonOkonomiOpplysningUtgift> opplysningUtgifter = new ArrayList<>();
        for (String utgiftstype: utgiftstyper) {
            if (utgiftstype.equals(BARNEHAGE_TYPE) || utgiftstype.equals(SFO_TYPE)) {
                oversiktUtgifter.add(new JsonOkonomioversiktUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"));
            } else if (utgiftstype.equals(FRITIDSAKTIVITETER_TYPE) || utgiftstype.equals(TANNREGULERING_TYPE)
                    || utgiftstype.equals(ANNET_TYPE)) {
                opplysningUtgifter.add(new JsonOkonomiOpplysningUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"));
            }
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(asList(new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(BEKREFTELSE_TYPE)
                .withVerdi(harUtgifter)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setUtgift(oversiktUtgifter);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtgift(opplysningUtgifter);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getFamilie().setForsorgerplikt(new JsonForsorgerplikt()
                .withHarForsorgerplikt(new JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(harForsorgerplikt)));
        return soknadUnderArbeid;
    }
}

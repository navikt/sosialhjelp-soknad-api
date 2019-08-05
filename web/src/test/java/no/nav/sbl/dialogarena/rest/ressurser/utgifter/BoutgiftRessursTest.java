package no.nav.sbl.dialogarena.rest.ressurser.utgifter;

import no.nav.sbl.dialogarena.rest.ressurser.utgifter.BoutgiftRessurs.BoutgifterFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
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
public class BoutgiftRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";
    private static final String BEKREFTELSE_TYPE = "boutgifter";
    private static final String HUSLEIE_TYPE = "husleie";
    private static final String STROM_TYPE = "strom";
    private static final String KOMMUNALAVGIFT_TYPE = "kommunalAvgift";
    private static final String OPPVARMING_TYPE = "oppvarming";
    private static final String BOLIGLAN_TYPE_1 = "boliglanAvdrag";
    private static final String BOLIGLAN_TYPE_2 = "boliglanRenter";
    private static final String ANNET_TYPE = "annenBoutgift";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private BoutgiftRessurs boutgiftRessurs;

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
    public void getBoutgifterSkalReturnereBekreftelseLikNullOgAltFalse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);
        
        assertThat(boutgifterFrontend.bekreftelse, nullValue());
        assertFalse(boutgifterFrontend.husleie);
        assertFalse(boutgifterFrontend.strom);
        assertFalse(boutgifterFrontend.oppvarming);
        assertFalse(boutgifterFrontend.kommunalAvgift);
        assertFalse(boutgifterFrontend.boliglan);
        assertFalse(boutgifterFrontend.annet);
    }

    @Test
    public void getBoutgifterSkalReturnereBekreftelserLikTrue(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBoutgifter(true, asList(HUSLEIE_TYPE, STROM_TYPE, KOMMUNALAVGIFT_TYPE,
                        OPPVARMING_TYPE, BOLIGLAN_TYPE_1, BOLIGLAN_TYPE_2, ANNET_TYPE)));

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertTrue(boutgifterFrontend.bekreftelse);
        assertTrue(boutgifterFrontend.husleie);
        assertTrue(boutgifterFrontend.strom);
        assertTrue(boutgifterFrontend.oppvarming);
        assertTrue(boutgifterFrontend.kommunalAvgift);
        assertTrue(boutgifterFrontend.boliglan);
        assertTrue(boutgifterFrontend.annet);
    }

    @Test
    public void putBoutgifterSkalSetteAltFalseDersomManVelgerHarIkkeBoutgifter(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBoutgifter(true, asList(HUSLEIE_TYPE, STROM_TYPE, KOMMUNALAVGIFT_TYPE,
                        ANNET_TYPE)));

        BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();
        boutgifterFrontend.setBekreftelse(false);
        boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse boutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertFalse(boutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBoutgifter.isEmpty());
        assertTrue(opplysningerBoutgifter.isEmpty());
    }

    @Test
    public void putBoutgifterSkalSetteAlleBekreftelserLikFalse(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBoutgifter(true, asList(HUSLEIE_TYPE, STROM_TYPE,
                        KOMMUNALAVGIFT_TYPE, ANNET_TYPE)));

        BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();
        boutgifterFrontend.setBekreftelse(false);
        boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse boutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertFalse(boutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBoutgifter.isEmpty());
        assertTrue(opplysningerBoutgifter.isEmpty());
    }

    @Test
    public void putBoutgifterSkalSetteNoenBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();
        boutgifterFrontend.setBekreftelse(true);
        boutgifterFrontend.setHusleie(true);
        boutgifterFrontend.setStrom(true);
        boutgifterFrontend.setOppvarming(false);
        boutgifterFrontend.setKommunalAvgift(false);
        boutgifterFrontend.setBoliglan(false);
        boutgifterFrontend.setAnnet(false);
        boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse boutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertThat(boutgiftBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(boutgiftBekreftelse.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(boutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(HUSLEIE_TYPE)));
        assertFalse(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(BOLIGLAN_TYPE_1)));
        assertFalse(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(BOLIGLAN_TYPE_2)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(STROM_TYPE)));
        assertFalse(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(KOMMUNALAVGIFT_TYPE)));
        assertFalse(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(OPPVARMING_TYPE)));
        assertFalse(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(ANNET_TYPE)));
    }

    @Test
    public void putBoutgifterSkalSetteAlleBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BoutgifterFrontend boutgifterFrontend = new BoutgifterFrontend();
        boutgifterFrontend.setBekreftelse(true);
        boutgifterFrontend.setHusleie(true);
        boutgifterFrontend.setStrom(true);
        boutgifterFrontend.setOppvarming(true);
        boutgifterFrontend.setKommunalAvgift(true);
        boutgifterFrontend.setBoliglan(true);
        boutgifterFrontend.setAnnet(true);
        boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse boutgiftBekreftelse = bekreftelser.get(0);
        List<JsonOkonomioversiktUtgift> oversiktBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOversikt().getUtgift();
        List<JsonOkonomiOpplysningUtgift> opplysningerBoutgifter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtgift();
        assertThat(boutgiftBekreftelse.getKilde(), is(JsonKilde.BRUKER));
        assertThat(boutgiftBekreftelse.getType(), is(BEKREFTELSE_TYPE));
        assertTrue(boutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(HUSLEIE_TYPE)));
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(BOLIGLAN_TYPE_1)));
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(BOLIGLAN_TYPE_2)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(STROM_TYPE)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(KOMMUNALAVGIFT_TYPE)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(OPPVARMING_TYPE)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(ANNET_TYPE)));
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithBoutgifter(Boolean harUtgifter, List<String> utgiftstyper) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomioversiktUtgift> oversiktUtgifter = new ArrayList<>();
        List<JsonOkonomiOpplysningUtgift> opplysningUtgifter = new ArrayList<>();
        for (String utgiftstype: utgiftstyper) {
            if (utgiftstype.equals(HUSLEIE_TYPE) || utgiftstype.equals(BOLIGLAN_TYPE_1) || utgiftstype.equals(BOLIGLAN_TYPE_2)) {
                oversiktUtgifter.add(new JsonOkonomioversiktUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"));
            } else if (utgiftstype.equals(STROM_TYPE) || utgiftstype.equals(OPPVARMING_TYPE)
                    || utgiftstype.equals(KOMMUNALAVGIFT_TYPE) || utgiftstype.equals(ANNET_TYPE)) {
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
        return soknadUnderArbeid;
    }
}

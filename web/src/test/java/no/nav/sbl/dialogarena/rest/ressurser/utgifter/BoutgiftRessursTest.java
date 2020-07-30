package no.nav.sbl.dialogarena.rest.ressurser.utgifter;

import no.nav.sbl.dialogarena.rest.ressurser.utgifter.BoutgiftRessurs.BoutgifterFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
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

import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
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
public class BoutgiftRessursTest {

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
    private BoutgiftRessurs boutgiftRessurs;

    @Before
    public void setUp() {
        when(subjectHandlerWrapper.getIdent()).thenReturn("123");
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
    }

    @Test
    public void getBoutgifterSkalReturnereBekreftelseLikNullOgAlleUnderverdierLikFalse(){
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
                createJsonInternalSoknadWithBoutgifter(true, asList(UTGIFTER_HUSLEIE, UTGIFTER_STROM, UTGIFTER_KOMMUNAL_AVGIFT,
                        UTGIFTER_OPPVARMING, UTGIFTER_BOLIGLAN_AVDRAG, UTGIFTER_BOLIGLAN_RENTER, UTGIFTER_ANNET_BO)));

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
    public void getBoutgifterSkalReturnereSkalViseInfoLikTrueDersomManHverkenHarBostotteSakerEllerUtbetalinger(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertTrue(boutgifterFrontend.skalViseInfoVedBekreftelse);
    }

    @Test
    public void getBoutgifterSkalReturnereSkalViseInfoLikFalseDersomManHarBostotteSakerEllerUtbetalinger(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBostotte(
                new JsonBostotte().withSaker(asList(new JsonBostotteSak().withType(UTBETALING_HUSBANKEN))));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtbetaling(
                asList(new JsonOkonomiOpplysningUtbetaling().withType(UTBETALING_HUSBANKEN)));
        setBekreftelse(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger(),
                BOSTOTTE_SAMTYKKE, true, "Test samtykke!");

        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertFalse(boutgifterFrontend.skalViseInfoVedBekreftelse);
    }

    @Test
    public void getBoutgifterSkalReturnereSkalViseInfoLikTrueDersomHusbankenErNedeOgManSvarerNeiTilBostotte(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setStotteFraHusbankenFeilet(true);
        setBekreftelse(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger(),
                BOSTOTTE_SAMTYKKE, true, "Test samtykke!");
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(
                asList(new JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE).withVerdi(false)));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertTrue(boutgifterFrontend.skalViseInfoVedBekreftelse);
    }

    @Test
    public void getBoutgifterSkalReturnereSkalViseInfoLikTrueDersomViMAnglerSamtykkeOgManSvarerNeiTilBostotte(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(
                asList(new JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE).withVerdi(false),
                        new JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE_SAMTYKKE).withVerdi(false)));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertTrue(boutgifterFrontend.skalViseInfoVedBekreftelse);
    }

    @Test
    public void putBoutgifterSkalSetteAltFalseDersomManVelgerHarIkkeBoutgifter(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBoutgifter(true, asList(UTGIFTER_HUSLEIE, UTGIFTER_STROM,
                        UTGIFTER_KOMMUNAL_AVGIFT, UTGIFTER_ANNET_BO)));

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
        assertThat(boutgiftBekreftelse.getType(), is(BEKREFTELSE_BOUTGIFTER));
        assertTrue(boutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_HUSLEIE)));
        assertFalse(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_AVDRAG)));
        assertFalse(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_RENTER)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_STROM)));
        assertFalse(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_KOMMUNAL_AVGIFT)));
        assertFalse(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_OPPVARMING)));
        assertFalse(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_ANNET_BO)));
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
        assertThat(boutgiftBekreftelse.getType(), is(BEKREFTELSE_BOUTGIFTER));
        assertTrue(boutgiftBekreftelse.getVerdi());
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_HUSLEIE)));
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_AVDRAG)));
        assertTrue(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_RENTER)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_STROM)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_KOMMUNAL_AVGIFT)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_OPPVARMING)));
        assertTrue(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_ANNET_BO)));
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
            if (utgiftstype.equals(UTGIFTER_HUSLEIE) || utgiftstype.equals(UTGIFTER_BOLIGLAN_AVDRAG) || utgiftstype.equals(UTGIFTER_BOLIGLAN_RENTER)) {
                oversiktUtgifter.add(new JsonOkonomioversiktUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"));
            } else if (utgiftstype.equals(UTGIFTER_STROM) || utgiftstype.equals(UTGIFTER_OPPVARMING)
                    || utgiftstype.equals(UTGIFTER_KOMMUNAL_AVGIFT) || utgiftstype.equals(UTGIFTER_ANNET_BO)) {
                opplysningUtgifter.add(new JsonOkonomiOpplysningUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"));
            }
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(asList(new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(BEKREFTELSE_BOUTGIFTER)
                .withVerdi(harUtgifter)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().setUtgift(oversiktUtgifter);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtgift(opplysningUtgifter);
        return soknadUnderArbeid;
    }
}

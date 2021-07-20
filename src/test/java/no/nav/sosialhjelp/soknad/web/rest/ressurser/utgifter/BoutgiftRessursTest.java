package no.nav.sosialhjelp.soknad.web.rest.ressurser.utgifter;

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.utgifter.BoutgiftRessurs.BoutgifterFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoutgiftRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private BoutgiftRessurs boutgiftRessurs;

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
    void getBoutgifterSkalReturnereBekreftelseLikNullOgAlleUnderverdierLikFalse(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);
        
        assertThat(boutgifterFrontend.bekreftelse).isNull();
        assertThat(boutgifterFrontend.husleie).isFalse();
        assertThat(boutgifterFrontend.strom).isFalse();
        assertThat(boutgifterFrontend.oppvarming).isFalse();
        assertThat(boutgifterFrontend.kommunalAvgift).isFalse();
        assertThat(boutgifterFrontend.boliglan).isFalse();
        assertThat(boutgifterFrontend.annet).isFalse();
    }

    @Test
    void getBoutgifterSkalReturnereBekreftelserLikTrue(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBoutgifter(true, asList(UTGIFTER_HUSLEIE, UTGIFTER_STROM, UTGIFTER_KOMMUNAL_AVGIFT,
                        UTGIFTER_OPPVARMING, UTGIFTER_BOLIGLAN_AVDRAG, UTGIFTER_BOLIGLAN_RENTER, UTGIFTER_ANNET_BO)));

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertThat(boutgifterFrontend.bekreftelse).isTrue();
        assertThat(boutgifterFrontend.husleie).isTrue();
        assertThat(boutgifterFrontend.strom).isTrue();
        assertThat(boutgifterFrontend.oppvarming).isTrue();
        assertThat(boutgifterFrontend.kommunalAvgift).isTrue();
        assertThat(boutgifterFrontend.boliglan).isTrue();
        assertThat(boutgifterFrontend.annet).isTrue();
    }

    @Test
    void getBoutgifterSkalReturnereSkalViseInfoLikTrueDersomManHverkenHarBostotteSakerEllerUtbetalinger(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isTrue();
    }

    @Test
    void getBoutgifterSkalReturnereSkalViseInfoLikFalseDersomManHarBostotteSakerEllerUtbetalinger(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBostotte(
                new JsonBostotte().withSaker(asList(new JsonBostotteSak().withType(UTBETALING_HUSBANKEN))));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtbetaling(
                asList(new JsonOkonomiOpplysningUtbetaling().withType(UTBETALING_HUSBANKEN)));
        setBekreftelse(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger(),
                BOSTOTTE_SAMTYKKE, true, "Test samtykke!");

        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isFalse();
    }

    @Test
    void getBoutgifterSkalReturnereSkalViseInfoLikTrueDersomHusbankenErNedeOgManSvarerNeiTilBostotte(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setStotteFraHusbankenFeilet(true);
        setBekreftelse(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger(),
                BOSTOTTE_SAMTYKKE, true, "Test samtykke!");
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(
                asList(new JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE).withVerdi(false)));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isTrue();
    }

    @Test
    void getBoutgifterSkalReturnereSkalViseInfoLikTrueDersomViMAnglerSamtykkeOgManSvarerNeiTilBostotte(){
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(
                asList(new JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE).withVerdi(false),
                        new JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE_SAMTYKKE).withVerdi(false)));
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknadUnderArbeid);

        BoutgifterFrontend boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID);

        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isTrue();
    }

    @Test
    void putBoutgifterSkalSetteAltFalseDersomManVelgerHarIkkeBoutgifter(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithBoutgifter(true, asList(UTGIFTER_HUSLEIE, UTGIFTER_STROM,
                        UTGIFTER_KOMMUNAL_AVGIFT, UTGIFTER_ANNET_BO)));
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");

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
        assertThat(boutgiftBekreftelse.getVerdi()).isFalse();
        assertThat(oversiktBoutgifter.isEmpty()).isTrue();
        assertThat(opplysningerBoutgifter.isEmpty()).isTrue();
    }

    @Test
    void putBoutgifterSkalSetteNoenBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");

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
        assertThat(boutgiftBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(boutgiftBekreftelse.getType()).isEqualTo(BEKREFTELSE_BOUTGIFTER);
        assertThat(boutgiftBekreftelse.getVerdi()).isTrue();
        assertThat(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_HUSLEIE))).isTrue();
        assertThat(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_AVDRAG))).isFalse();
        assertThat(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_RENTER))).isFalse();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_STROM))).isTrue();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_KOMMUNAL_AVGIFT))).isFalse();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_OPPVARMING))).isFalse();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_ANNET_BO))).isFalse();
    }

    @Test
    void putBoutgifterSkalSetteAlleBekreftelser(){
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");

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
        assertThat(boutgiftBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(boutgiftBekreftelse.getType()).isEqualTo(BEKREFTELSE_BOUTGIFTER);
        assertThat(boutgiftBekreftelse.getVerdi()).isTrue();
        assertThat(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_HUSLEIE))).isTrue();
        assertThat(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_AVDRAG))).isTrue();
        assertThat(oversiktBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_BOLIGLAN_RENTER))).isTrue();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_STROM))).isTrue();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_KOMMUNAL_AVGIFT))).isTrue();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_OPPVARMING))).isTrue();
        assertThat(opplysningerBoutgifter.stream().anyMatch(boutgift -> boutgift.getType().equals(UTGIFTER_ANNET_BO))).isTrue();
    }

    @Test
    void getBoutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> boutgiftRessurs.hentBoutgifter(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void putBoutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());

        var boutgifterFrontend = new BoutgifterFrontend();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend));

        verifyNoInteractions(soknadUnderArbeidRepository);
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

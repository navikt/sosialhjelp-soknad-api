package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt.UtbetalingRessurs.UtbetalingerFrontend;
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
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE;
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
public class UtbetalingRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private TextService textService;

    @InjectMocks
    private UtbetalingRessurs utbetalingRessurs;

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
    public void getUtbetalingerSkalReturnereBekreftelseLikNullOgAltFalse() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        UtbetalingerFrontend utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID);

        assertThat(utbetalingerFrontend.bekreftelse).isNull();
        assertThat(utbetalingerFrontend.forsikring).isFalse();
        assertThat(utbetalingerFrontend.salg).isFalse();
        assertThat(utbetalingerFrontend.utbytte).isFalse();
        assertThat(utbetalingerFrontend.annet).isFalse();
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet).isNull();
    }

    @Test
    public void getUtbetalingerSkalReturnereBekreftelserLikTrue() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING,
                        UTBETALING_ANNET), null));

        UtbetalingerFrontend utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID);

        assertThat(utbetalingerFrontend.bekreftelse).isTrue();
        assertThat(utbetalingerFrontend.utbytte).isTrue();
        assertThat(utbetalingerFrontend.salg).isTrue();
        assertThat(utbetalingerFrontend.forsikring).isTrue();
        assertThat(utbetalingerFrontend.annet).isTrue();
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet).isNull();
    }

    @Test
    public void getUtbetalingerSkalReturnereBeskrivelseAvAnnet() {
        String beskrivelse = "Lottogevinst";
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_ANNET), beskrivelse));

        UtbetalingerFrontend utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID);

        assertThat(utbetalingerFrontend.bekreftelse).isTrue();
        assertThat(utbetalingerFrontend.annet).isTrue();
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet).isEqualTo(beskrivelse);
    }

    @Test
    public void putUtbetalingerSkalSetteAltFalseDersomManVelgerHarIkkeUtbetalinger() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING,
                        UTBETALING_ANNET), "Lottogevinst"));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalingBekreftelse.getVerdi()).isFalse();
        assertThat(utbetalinger).isEmpty();
    }

    @Test
    public void putUtbetalingerSkalSetteAlleBekreftelserLikFalse() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_UTBYTTE, UTBETALING_SALG,
                        UTBETALING_FORSIKRING, UTBETALING_ANNET), "Lottogevinst"));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling();
        assertThat(utbetalingBekreftelse.getVerdi()).isFalse();
        assertThat(utbetalinger).isEmpty();
        assertThat(beskrivelse).isBlank();
    }

    @Test
    public void putUtbetalingerSkalSetteNoenBekreftelser() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(true);
        utbetalingerFrontend.setForsikring(true);
        utbetalingerFrontend.setSalg(true);
        utbetalingerFrontend.setUtbytte(false);
        utbetalingerFrontend.setAnnet(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalingBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetalingBekreftelse.getType()).isEqualTo(BEKREFTELSE_UTBETALING);
        assertThat(utbetalingBekreftelse.getVerdi()).isTrue();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_FORSIKRING))).isTrue();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_SALG))).isTrue();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_UTBYTTE))).isFalse();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_ANNET))).isFalse();
    }

    @Test
    public void putUtbetalingerSkalSetteAlleBekreftelser() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(true);
        utbetalingerFrontend.setForsikring(true);
        utbetalingerFrontend.setSalg(true);
        utbetalingerFrontend.setUtbytte(true);
        utbetalingerFrontend.setAnnet(true);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getUtbetaling();
        assertThat(utbetalingBekreftelse.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetalingBekreftelse.getType()).isEqualTo(BEKREFTELSE_UTBETALING);
        assertThat(utbetalingBekreftelse.getVerdi()).isTrue();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_FORSIKRING))).isTrue();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_SALG))).isTrue();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_UTBYTTE))).isTrue();
        assertThat(utbetalinger.stream().anyMatch(utbetaling -> utbetaling.getType().equals(UTBETALING_ANNET))).isTrue();
    }

    @Test
    public void putUtbetalingerSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet() {
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(anyString());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithUtbetalinger(true, asList(UTBETALING_ANNET), "Lottogevinst"));

        UtbetalingerFrontend utbetalingerFrontend = new UtbetalingerFrontend();
        utbetalingerFrontend.setBekreftelse(false);
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend);

        SoknadUnderArbeid soknadUnderArbeid = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        List<JsonOkonomibekreftelse> bekreftelser = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBekreftelse();
        JsonOkonomibekreftelse utbetalingBekreftelse = bekreftelser.get(0);
        String beskrivelse = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData()
                .getOkonomi().getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling();
        assertThat(utbetalingBekreftelse.getVerdi()).isFalse();
        assertThat(beskrivelse).isBlank();
    }

    @Test
    public void getUtbetalingerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    public void putUtbetalingerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        var utbetalingerFrontend = new UtbetalingerFrontend();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithUtbetalinger(Boolean harUtbetalinger, List<String> utbetalingTyper, String beskrivelseAvAnnet) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = new ArrayList<>();
        for (String utbetaling : utbetalingTyper) {
            utbetalinger.add(new JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(utbetaling)
                    .withTittel("tittel"));
        }
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBekreftelse(asList(new JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(BEKREFTELSE_UTBETALING)
                .withVerdi(harUtbetalinger)));
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setUtbetaling(utbetalinger);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().setBeskrivelseAvAnnet(
                new JsonOkonomibeskrivelserAvAnnet().withUtbetaling(beskrivelseAvAnnet));
        return soknadUnderArbeid;
    }
}

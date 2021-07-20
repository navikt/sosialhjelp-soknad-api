package no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.inntekt.SkattbarInntektRessurs.SkattbarInntektFrontend;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkattbarInntektRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SkattetatenSystemdata skattetatenSystemdata;

    @Mock
    private TextService textService;

    @InjectMocks
    private SkattbarInntektRessurs skattbarInntektRessurs;

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
    void getSkattbarInntektSkalReturnereTomListe() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        SkattbarInntektFrontend skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID);

        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten).isEmpty();
    }

    @Test
    void getSkattbarInntektSkalReturnereBekreftetSkattbarInntekt() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSkattbarInntekt(true));

        SkattbarInntektFrontend skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID);

        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten).hasSize(1);
    }

    @Test
    void getSkattbarInntektSkalReturnereHarIkkeSkattbarInntekt() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSkattbarInntekt(false));

        SkattbarInntektFrontend skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID);

        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten).isEmpty();
    }

    @Test
    void skattbarInntekt_skalGiSamtykke() {
        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSkattbarInntekt(false);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(any());

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, true, "token");

        // Sjekker kaller til bostotteSystemdata
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(skattetatenSystemdata).updateSystemdataIn(argument.capture());
        JsonOkonomi okonomi = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        JsonOkonomibekreftelse fangetBekreftelse = okonomi.getOpplysninger().getBekreftelse().get(0);
        assertThat(fangetBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        assertThat(fangetBekreftelse.getVerdi()).isTrue();

        // Sjekker lagring av soknaden
        SoknadUnderArbeid spartSoknad = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        assertThat(spartSoknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse()).hasSize(1);
        JsonOkonomibekreftelse spartBekreftelse = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse().get(0);
        assertThat(spartBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        assertThat(spartBekreftelse.getVerdi()).isTrue();
    }

    @Test
    void skattbarInntekt_skalTaBortSamtykke() {
        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSkattbarInntekt(false);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        setBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE, true, "");
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(any());

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, false, "token");

        // Sjekker kaller til skattbarInntektSystemdata
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(skattetatenSystemdata).updateSystemdataIn(argument.capture());
        JsonOkonomi okonomi = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        JsonOkonomibekreftelse fangetBekreftelse = okonomi.getOpplysninger().getBekreftelse().get(0);
        assertThat(fangetBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        assertThat(fangetBekreftelse.getVerdi()).isFalse();

        // Sjekker lagring av soknaden
        SoknadUnderArbeid spartSoknad = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonOkonomiopplysninger sparteOpplysninger = spartSoknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        assertThat(sparteOpplysninger.getBekreftelse()).hasSize(1);
        JsonOkonomibekreftelse spartBekreftelse = sparteOpplysninger.getBekreftelse().get(0);
        assertThat(spartBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        assertThat(spartBekreftelse.getVerdi()).isFalse();
    }

    @Test
    void skattbarInntekt_skalIkkeForandreSamtykke() {
        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSkattbarInntekt(false);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);
        doNothing().when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(any());

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, false, "token");

        // Sjekker kaller til skattbarInntektSystemdata
        verify(skattetatenSystemdata, times(0)).updateSystemdataIn(any());

        // Sjekker lagring av soknaden
        verify(soknadUnderArbeidRepository, times(0)).oppdaterSoknadsdata(any(), anyString());

        // Sjekker soknaden
        assertThat(soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse()).isEmpty();
    }

    @Test
    void getSkattbarInntektkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerHarTilgang();

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    @Test
    void putSamtykkeSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        doThrow(new AuthorizationException("Not for you my friend")).when(tilgangskontroll).verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID);

        assertThatExceptionOfType(AuthorizationException.class)
                .isThrownBy(() -> skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, true, "token"));

        verifyNoInteractions(soknadUnderArbeidRepository);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithSkattbarInntekt(Boolean harSkattbarInntekt) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        if (harSkattbarInntekt) {
            JsonOkonomiOpplysningUtbetaling utbetaling = new JsonOkonomiOpplysningUtbetaling()
                    .withType(UTBETALING_SKATTEETATEN)
                    .withKilde(JsonKilde.SYSTEM)
                    .withTittel("Utbetalingen!")
                    .withBelop(123456);
            soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling().add(utbetaling);
        }
        return soknadUnderArbeid;
    }
}

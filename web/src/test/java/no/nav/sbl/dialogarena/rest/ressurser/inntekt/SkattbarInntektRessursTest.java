package no.nav.sbl.dialogarena.rest.ressurser.inntekt;

import no.nav.sbl.dialogarena.rest.ressurser.inntekt.SkattbarInntektRessurs.SkattbarInntektFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.setBekreftelse;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SkattbarInntektRessursTest {

    private static final String BEHANDLINGSID = "123";
    private static final String EIER = "123456789101";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @SuppressWarnings("unused")
    @Mock
    private Tilgangskontroll tilgangskontroll;

    @Mock
    private SkattetatenSystemdata skattetatenSystemdata;

    @Mock
    private TextService textService;

    @InjectMocks
    private SkattbarInntektRessurs skattbarInntektRessurs;

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
    public void getSkattbarInntektSkalReturnereTomListe(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)));

        SkattbarInntektFrontend skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID);

        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten.size(), is(0));
    }

    @Test
    public void getSkattbarInntektSkalReturnereBekreftetSkattbarInntekt(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSkattbarInntekt(true));

        SkattbarInntektFrontend skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID);

        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten.size(), is(1));
    }

    @Test
    public void getSkattbarInntektSkalReturnereHarIkkeSkattbarInntekt(){
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(
                createJsonInternalSoknadWithSkattbarInntekt(false));

        SkattbarInntektFrontend skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID);

        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten.size(), is(0));
    }

    @Test
    public void skattbarInntekt_skalGiSamtykke() {
        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSkattbarInntekt(false);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, true, "token");

        // Sjekker kaller til bostotteSystemdata
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(skattetatenSystemdata).updateSystemdataIn(argument.capture(), anyString());
        JsonOkonomi okonomi = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        JsonOkonomibekreftelse fangetBekreftelse = okonomi.getOpplysninger().getBekreftelse().get(0);
        Assertions.assertThat(fangetBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        Assertions.assertThat(fangetBekreftelse.getVerdi()).isTrue();

        // Sjekker lagring av soknaden
        SoknadUnderArbeid spartSoknad = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        Assertions.assertThat(spartSoknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse()).hasSize(1);
        JsonOkonomibekreftelse spartBekreftelse = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse().get(0);
        Assertions.assertThat(spartBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        Assertions.assertThat(spartBekreftelse.getVerdi()).isTrue();
    }

    @Test
    public void skattbarInntekt_skalTaBortSamtykke() {
        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSkattbarInntekt(false);
        JsonOkonomiopplysninger opplysninger = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        setBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE, true, "");
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, false, "token");

        // Sjekker kaller til skattbarInntektSystemdata
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(skattetatenSystemdata).updateSystemdataIn(argument.capture(), anyString());
        JsonOkonomi okonomi = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        JsonOkonomibekreftelse fangetBekreftelse = okonomi.getOpplysninger().getBekreftelse().get(0);
        Assertions.assertThat(fangetBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        Assertions.assertThat(fangetBekreftelse.getVerdi()).isFalse();

        // Sjekker lagring av soknaden
        SoknadUnderArbeid spartSoknad = catchSoknadUnderArbeidSentToOppdaterSoknadsdata();
        JsonOkonomiopplysninger sparteOpplysninger = spartSoknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger();
        Assertions.assertThat(sparteOpplysninger.getBekreftelse()).hasSize(1);
        JsonOkonomibekreftelse spartBekreftelse = sparteOpplysninger.getBekreftelse().get(0);
        Assertions.assertThat(spartBekreftelse.getType()).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE);
        Assertions.assertThat(spartBekreftelse.getVerdi()).isFalse();
    }

    @Test
    public void skattbarInntekt_skalIkkeForandreSamtykke() {
        SoknadUnderArbeid soknad = createJsonInternalSoknadWithSkattbarInntekt(false);
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(soknad);

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, false, "token");

        // Sjekker kaller til skattbarInntektSystemdata
        verify(skattetatenSystemdata, times(0)).updateSystemdataIn(any(), anyString());

        // Sjekker lagring av soknaden
        verify(soknadUnderArbeidRepository, times(0)).oppdaterSoknadsdata(any(), anyString());

        // Sjekker soknaden
        Assertions.assertThat(soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse()).hasSize(0);
    }

    private SoknadUnderArbeid catchSoknadUnderArbeidSentToOppdaterSoknadsdata() {
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).oppdaterSoknadsdata(argument.capture(), anyString());
        return argument.getValue();
    }

    private SoknadUnderArbeid createJsonInternalSoknadWithSkattbarInntekt(Boolean harSkattbarInntekt) {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        if(harSkattbarInntekt) {
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

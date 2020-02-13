package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceTest {

    private static final String EIER = "Hans og Grete";
    private static final String BEHANDLINGSID = "123";

    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private OppgaveHandterer oppgaveHandterer;
    @Mock
    private SystemdataUpdater systemdataUpdater;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private InnsendingService innsendingService;
    @Mock
    private TextService textService;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private SoknadService soknadService;


    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
    }

    @Test
    public void skalStarteSoknad() {
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString())).thenReturn("123");
        soknadService.startSoknad("");

        String bruker = OidcFeatureToggleUtils.getUserId();
        verify(henvendelsesConnector).startSoknad(eq(bruker));
        ArgumentCaptor<SoknadUnderArbeid> argument = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        verify(soknadUnderArbeidRepository).opprettSoknad(argument.capture(), eq(bruker));
        List<JsonOkonomibekreftelse> bekreftelser = argument.getValue().getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse();
        assertThat(bekreftelser.stream().anyMatch(bekreftelse -> harBekreftelseFor(bekreftelse, UTBETALING_SKATTEETATEN_SAMTYKKE))).isFalse();
        assertThat(bekreftelser.stream().anyMatch(bekreftelse -> harBekreftelseFor(bekreftelse, BOSTOTTE_SAMTYKKE))).isFalse();
        DateTimeUtils.setCurrentMillisSystem();
    }

    private boolean harBekreftelseFor(JsonOkonomibekreftelse bekreftelse, String bekreftelsesType) {
        return bekreftelse.getVerdi() && bekreftelse.getType().equalsIgnoreCase(bekreftelsesType);
    }

    @Test
    public void skalSendeSoknad() {
        String testType = "testType";
        String testTilleggsinfo = "testTilleggsinfo";
        String testType2 = "testType2";
        String testTilleggsinfo2 = "testTilleggsinfo2";
        List<JsonVedlegg> jsonVedlegg = asList(
                new JsonVedlegg()
                        .withType(testType)
                        .withTilleggsinfo(testTilleggsinfo)
                        .withStatus(Vedleggstatus.LastetOpp.toString()),
                new JsonVedlegg()
                        .withType(testType2)
                        .withTilleggsinfo(testTilleggsinfo2)
                        .withStatus(Vedleggstatus.LastetOpp.toString()));

        String behandlingsId = "123";
        String aktorId = "123456";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(aktorId));
        soknadUnderArbeid.getJsonInternalSoknad().setVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg));
        when(soknadUnderArbeidRepository.hentSoknad(eq(behandlingsId), anyString())).thenReturn(soknadUnderArbeid);

        soknadService.sendSoknad(behandlingsId);

        ArgumentCaptor<SoknadUnderArbeid> soknadUnderArbeidCaptor = ArgumentCaptor.forClass(SoknadUnderArbeid.class);
        ArgumentCaptor<VedleggMetadataListe> vedleggCaptor = ArgumentCaptor.forClass(VedleggMetadataListe.class);
        verify(henvendelsesConnector, atLeastOnce()).oppdaterMetadataVedAvslutningAvSoknad(eq(behandlingsId), vedleggCaptor.capture(), soknadUnderArbeidCaptor.capture(), eq(false));
        verify(oppgaveHandterer).leggTilOppgave(eq(behandlingsId), anyString());

        SoknadUnderArbeid capturedSoknadUnderArbeid = soknadUnderArbeidCaptor.getValue();
        assertThat(capturedSoknadUnderArbeid).isEqualTo(soknadUnderArbeid);

        VedleggMetadataListe capturedVedlegg = vedleggCaptor.getValue();
        assertThat(capturedVedlegg.vedleggListe).hasSize(2);
        assertThat(capturedVedlegg.vedleggListe.get(0).filnavn).isEqualTo(testType);
        assertThat(capturedVedlegg.vedleggListe.get(0).skjema).isEqualTo(testType);
        assertThat(capturedVedlegg.vedleggListe.get(0).tillegg).isEqualTo(testTilleggsinfo);
        assertThat(capturedVedlegg.vedleggListe.get(1).filnavn).isEqualTo(testType2);
        assertThat(capturedVedlegg.vedleggListe.get(1).skjema).isEqualTo(testType2);
        assertThat(capturedVedlegg.vedleggListe.get(1).tillegg).isEqualTo(testTilleggsinfo2);
    }

    @Test
    public void skalAvbryteSoknad() {
        when(soknadUnderArbeidRepository.hentSoknadOptional(eq(BEHANDLINGSID), anyString())).thenReturn(
                Optional.of(new SoknadUnderArbeid()
                        .withBehandlingsId(BEHANDLINGSID)
                        .withVersjon(1L)
                        .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))));

        soknadService.avbrytSoknad(BEHANDLINGSID);

        verify(henvendelsesConnector).avbrytSoknad(BEHANDLINGSID, false);
        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }
}
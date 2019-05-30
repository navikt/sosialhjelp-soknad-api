package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadataListe;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.SKJEMANUMMER;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadDataFletterTest {

    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private OppgaveHandterer oppgaveHandterer;
    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;
    @Mock
    private SystemdataUpdater systemdataUpdater;
    @Mock
    SoknadMetricsService soknadMetricsService;
    @Mock
    private SoknadUnderArbeidService soknadUnderArbeidService;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private SoknadDataFletter soknadDataFletter;


    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        when(kravdialogInformasjonHolder.hentAlleSkjemanumre()).thenReturn(new KravdialogInformasjonHolder().hentAlleSkjemanumre());
        when(kravdialogInformasjonHolder.hentKonfigurasjon(anyString())).thenReturn(new KravdialogInformasjonHolder().getSoknadsKonfigurasjoner().get(0));
    }

    @Test
    public void skalStarteSoknad() {
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
        when(henvendelsesConnector.startSoknad(anyString(), anyString(), anyString(), any(SoknadType.class))).thenReturn("123");
        soknadDataFletter.startSoknad(SKJEMANUMMER);

        ArgumentCaptor<String> uid = ArgumentCaptor.forClass(String.class);
        String bruker = OidcFeatureToggleUtils.getUserId();
        verify(henvendelsesConnector).startSoknad(eq(bruker), eq(SKJEMANUMMER), uid.capture(), any(SoknadType.class));
        verify(soknadUnderArbeidService).oppdaterEllerOpprettSoknadUnderArbeid(any(SoknadUnderArbeid.class), eq(bruker));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    @Ignore("Denne må utvides med søknadsosialhjelp sine vedleggsforventninger og faktum.")
    public void skalSendeSoknad() {
        String testType = "testType";
        String testTilleggsinfo = "testTilleggsinfo";
        String testType2 = "testType2";
        String testTilleggsinfo2 = "testTilleggsinfo2";
        List<JsonVedlegg> jsonVedlegg = asList(
                new JsonVedlegg()
                        .withType(testType)
                        .withTilleggsinfo(testTilleggsinfo)
                        .withStatus(Vedleggstatus.Status.LastetOpp.toString()),
                new JsonVedlegg()
                        .withType(testType2)
                        .withTilleggsinfo(testTilleggsinfo2)
                        .withStatus(Vedleggstatus.Status.LastetOpp.toString()));

        String behandlingsId = "123";
        String aktorId = "123456";
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(aktorId));
        soknadUnderArbeid.getJsonInternalSoknad().setVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg));
        when(soknadUnderArbeidRepository.hentSoknad(behandlingsId, anyString())).thenReturn(Optional.of(soknadUnderArbeid));

        when(kravdialogInformasjonHolder.hentKonfigurasjon(SKJEMANUMMER)).thenReturn(new KravdialogInformasjonHolder().hentKonfigurasjon(SKJEMANUMMER));
        soknadDataFletter.sendSoknad(behandlingsId);

        ArgumentCaptor<HovedskjemaMetadata> hovedCaptor = ArgumentCaptor.forClass(HovedskjemaMetadata.class);
        ArgumentCaptor<VedleggMetadataListe> vedleggCaptor = ArgumentCaptor.forClass(VedleggMetadataListe.class);
        verify(oppgaveHandterer).leggTilOppgave(eq(behandlingsId), eq(aktorId));

        HovedskjemaMetadata capturedHoved = hovedCaptor.getValue();
        assertThat(capturedHoved.filnavn).isEqualTo(SKJEMANUMMER);
        assertThat(capturedHoved.mimetype).isEqualTo("application/pdf");
        assertThat(capturedHoved.filStorrelse).isEqualTo("3");
        assertThat(capturedHoved.alternativRepresentasjon.get(0).mimetype).isEqualTo("application/pdf-fullversjon");

        VedleggMetadataListe capturedVedlegg = vedleggCaptor.getValue();
        assertThat(capturedVedlegg.vedleggListe).hasSize(3);
        assertThat(capturedVedlegg.vedleggListe.get(0).filnavn).isEqualTo(testType);
        assertThat(capturedVedlegg.vedleggListe.get(2).skjema).isEqualTo(Kodeverk.KVITTERING);
    }

}
package no.nav.sosialhjelp.soknad.business;

import no.nav.sosialhjelp.soknad.business.batch.oppgave.OppgaveHandterer;
import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.business.db.TestSupport;
import no.nav.sosialhjelp.soknad.business.db.config.DatabaseTestContext;
import no.nav.sosialhjelp.soknad.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.pdf.HandleBarKjoerer;
import no.nav.sosialhjelp.soknad.business.pdf.HtmlGenerator;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.TextHelpers;
import no.nav.sosialhjelp.soknad.business.sendtsoknad.SendtSoknadRepository;
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService;
import no.nav.sosialhjelp.soknad.business.service.OpplastetVedleggService;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.business.service.digisosapi.DigisosApiService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.EttersendingService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SystemdataUpdater;
import no.nav.sosialhjelp.soknad.business.service.systemdata.BostotteSystemdata;
import no.nav.sosialhjelp.soknad.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.consumer.bostotte.Bostotte;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonConsumer;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonService;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektService;
import no.nav.sosialhjelp.soknad.consumer.virusscan.VirusScanner;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.time.Clock;

import static org.mockito.Mockito.mock;

@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
@Configuration
public class SoknadServiceIntegrationTestContext {
    @Inject
    private DataSource dataSource;

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return new HenvendelseService();
    }

    @Bean
    public SoknadMetadataRepository soknadMetadataRepository() {
        return mock(SoknadMetadataRepository.class);
    }

    @Bean
    public OpplastetVedleggService opplastetVedleggService() {
        return new OpplastetVedleggService();
    }

    @Bean
    public RepositoryTestSupport testSupport() {
        return new TestSupport(dataSource);
    }

    @Bean
    public KodeverkService kodeverkService() {
        return mock(KodeverkService.class);
    }

    @Bean
    public SoknadService soknadService() {
        return new SoknadService();
    }

    @Bean
    public DigisosApiService digisosApiService() {
        return new DigisosApiService();
    }

    @Bean
    public DigisosApi digisosApi() {
        return mock(DigisosApi.class);
    }

    @Bean
    public HtmlGenerator pdfTemplate() {
        return new HandleBarKjoerer();
    }

    @Bean
    public NavMessageSource navMessageSource() {
        return new NavMessageSource();
    }

    @Bean
    public EttersendingService ettersendingService() {
        return new EttersendingService();
    }

    @Bean
    public SoknadMetricsService metricsService() {
        return mock(SoknadMetricsService.class);
    }

    @Bean
    public OppgaveHandterer oppgaveHandterer() {
        return mock(OppgaveHandterer.class);
    }

    @Bean
    public SoknadUnderArbeidRepository soknadUnderArbeidRepository() {
        return mock(SoknadUnderArbeidRepository.class);
    }

    @Bean
    OpplastetVedleggRepository opplastetVedleggRepository() {
        return mock(OpplastetVedleggRepository.class);
    }

    @Bean
    InnsendingService innsendingService() {
        return mock(InnsendingService.class);
    }

    @Bean
    SendtSoknadRepository sendtSoknadRepository() {
        return mock(SendtSoknadRepository.class);
    }

    @Bean
    SoknadUnderArbeidService soknadUnderArbeidService() {
        return mock(SoknadUnderArbeidService.class);
    }

    @Bean
    SystemdataUpdater systemdataUpdater() {
        return mock(SystemdataUpdater.class);
    }

    @Bean
    Systemdata systemdata() {
        return mock(Systemdata.class);
    }

    @Bean
    VirusScanner virusScanner() {
        return mock(VirusScanner.class);
    }

    @Bean
    SosialhjelpPdfGenerator sosialhjelpPdfGenerator() {
        return mock(SosialhjelpPdfGenerator.class);
    }

    @Bean
    TextHelpers textHelpers() {
        return mock(TextHelpers.class);
    }

    @Bean
    TextService textService() {
        return mock(TextService.class);
    }

    @Bean
    public BostotteSystemdata bostotteSystemdata() {
        return mock(BostotteSystemdata.class);
    }

    @Bean
    public Bostotte bostotte() {
        return mock(Bostotte.class);
    }

    @Bean
    public SkattetatenSystemdata skattetatenSystemdata() {
        return mock(SkattetatenSystemdata.class);
    }

    @Bean
    public SkattbarInntektService skattbarInntektService() {
        return mock(SkattbarInntektService.class);
    }

    @Bean
    public OrganisasjonService organisasjonService() {
        return mock(OrganisasjonService.class);
    }

    @Bean
    public OrganisasjonConsumer organisasjonConsumer() {
        return mock(OrganisasjonConsumer.class);
    }
}
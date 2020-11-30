package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DigisosApi;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.OpplastetVedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.DigisosApiService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BostotteSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.Bostotte;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.virusscan.VirusScanner;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.pdf.HandleBarKjoerer;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.TextHelpers;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.beans.factory.annotation.Autowire;
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
    public Kodeverk kodeverk() {
        return mock(Kodeverk.class);
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

    @Bean(autowire = Autowire.NO)
    SystemdataUpdater systemdataUpdater() {
        return mock(SystemdataUpdater.class);
    }

    @Bean(autowire = Autowire.NO)
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
    Adressekodeverk adressekodeverk() {
        return mock(Adressekodeverk.class);
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
package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.rest.actions.SoknadActions;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.DigisosApiService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.BostotteSystemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.SkattetatenSystemdata;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGeneratorConfig;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sbl.sosialhjelp.pdfmedpdfbox.TextHelpers;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.consumer.bostotte.Bostotte;
import no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApi;
import no.nav.sosialhjelp.soknad.consumer.fiks.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonConsumer;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonService;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektService;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

import static org.mockito.Mockito.mock;

@Configuration
@Import({DummyHolderConfig.class, PdfGeneratorConfig.class})
public class SoknadActionsTestConfig {

    @Bean
    public NavMessageSource tekster() {
        return mock(NavMessageSource.class);
    }

    @Bean
    public SoknadService soknadService() {
        return mock(SoknadService.class);
    }

    @Bean
    public DigisosApi DigisosApi() {
        return mock(DigisosApi.class);
    }

    @Bean
    public KommuneInfoService KommuneInfoService() {
        return mock(KommuneInfoService.class);
    }

    @Bean
    public DigisosApiService DigisosApiService() {
        return mock(DigisosApiService.class);
    }

    @Bean
    public OppgaveHandterer oppgaveHandterer() {
        return mock(OppgaveHandterer.class);
    }

    @Bean
    public InnsendingService innsendingService() {
        return mock(InnsendingService.class);
    }

    @Bean
    public HtmlGenerator pdfTemplate() {
        return mock(HtmlGenerator.class);
    }

    @Bean
    public SoknadActions soknadActions() {
        return new SoknadActions();
    }

    @Bean
    public SosialhjelpPdfGenerator sosialhjelpPdfGenerator() {
        return mock(SosialhjelpPdfGenerator.class);
    }

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return mock(Tilgangskontroll.class);
    }

    @Bean
    public SoknadMetadataRepository soknadMetadataRepository() {
        return mock(SoknadMetadataRepository.class);
    }

    @Bean
    public SoknadMetricsService soknadMetricsService() {
        return mock(SoknadMetricsService.class);
    }

    @Bean
    public SoknadUnderArbeidRepository soknadUnderArbeidRepository() {
        return mock(SoknadUnderArbeidRepository.class);
    }

    @Bean
    public TextHelpers textHelpers() {
        return mock(TextHelpers.class);
    }

    @Bean
    public TextService textService() {
        return mock(TextService.class);
    }

    @Bean
    public KodeverkService kodeverkService() {
        return mock(KodeverkService.class);
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return new HenvendelseService();
    }

    @Bean
    public Clock clock() {
        return mock(Clock.class);
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return mock(TransactionTemplate.class);
    }

    @Bean
    public SendtSoknadRepository sendtSoknadRepository() {
        return mock(SendtSoknadRepository.class);
    }

    @Bean
    public OpplastetVedleggRepository opplastetVedleggRepository() {
        return mock(OpplastetVedleggRepository.class);
    }

    @Bean
    public SoknadUnderArbeidService soknadUnderArbeidService() {
        return mock(SoknadUnderArbeidService.class);
    }

    @Bean
    public EttersendingService ettersendingService() {
        return mock(EttersendingService.class);
    }

    @Bean
    public SystemdataUpdater systemdataUpdater() {
        return mock(SystemdataUpdater.class);
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

    @Bean
    public Systemdata systemdata() {
        return mock(Systemdata.class);
    }
}

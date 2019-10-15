package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.rest.actions.SoknadActions;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.DigisosApi;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@Configuration
@Import(DummyHolderConfig.class)
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
    public OppgaveHandterer oppgaveHandterer() {
        return mock(OppgaveHandterer.class);
    }

    @Bean
    public InnsendingService innsendingService() {
        return null;
    }

    @Bean
    public SystemdataUpdater systemdataUpdater() {
        return null;
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
    public PDFService pdfService() {
        return new PDFService();
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
}

package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

import static org.mockito.Mockito.mock;

@Configuration
public class MockMvcSoknadRessursTestConfig {

    @Bean
    public SoknadService soknadService() {
        return mock(SoknadService.class);
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return mock(HenvendelseService.class);
    }

    @Bean
    public SoknadMetadataRepository soknadMetadataRepository() {
        return mock(SoknadMetadataRepository.class);
    }

    @Bean
    public Clock clock() {
        return mock(Clock.class);
    }

    @Bean
    public OppgaveHandterer oppgaveHandterer() {
        return mock(OppgaveHandterer.class);
    }

    @Bean
    public SoknadMetricsService soknadMetricsService() {
        return mock(SoknadMetricsService.class);
    }

    @Bean
    public InnsendingService innsendingService() {
        return mock(InnsendingService.class);
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
    public SoknadUnderArbeidRepository soknadUnderArbeidRepository() {
        return mock(SoknadUnderArbeidRepository.class);
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
    public Systemdata systemdata() {
        return mock(Systemdata.class);
    }

    @Bean
    public HtmlGenerator htmlGenerator() {
        return mock(HtmlGenerator.class);
    }

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return mock(Tilgangskontroll.class);
    }
}

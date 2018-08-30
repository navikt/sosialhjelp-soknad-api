package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;

import static org.mockito.Mockito.mock;

@Configuration
@Import({VedleggServiceIntegrationMockContext.class})
public class VedleggServiceIntegrationContext {

    @Bean
    public Clock clock(){ return Clock.systemDefaultZone(); }

    @Bean
    public VedleggService vedleggService() {
        return new VedleggService();
    }

    @Bean
    public FaktaService faktaService() {
        return new FaktaService();
    }

    @Bean
    public FillagerRepository fillagerRepository() {
        return mock(FillagerRepository.class);
    }

    @Bean
    public SoknadMetadataRepository soknadMetadataRepository() {
        return mock(SoknadMetadataRepository.class);
    }

    @Bean
    public HendelseRepository hendelseRepository() {
        return new HendelseRepositoryJdbc();
    }

    @Bean
    public OppgaveHandterer oppgaveHandterer() {
        return mock(OppgaveHandterer.class);
    }
}

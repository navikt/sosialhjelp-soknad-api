package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@Configuration
@Import({VedleggServiceIntegrationMockContext.class})
public class VedleggServiceIntegrationContext {

    @Bean
    public VedleggService vedleggService() {
        return new VedleggService();
    }

    @Bean
    public FaktaService faktaService() {
        return new FaktaService();
    }

    @Bean
    public HenvendelsePortType henvendelseEndpoint() {
        return mock(HenvendelsePortType.class);
    }

    @Bean
    public FillagerRepository fillagerRepository() {
        return mock(FillagerRepository.class);
    }

}

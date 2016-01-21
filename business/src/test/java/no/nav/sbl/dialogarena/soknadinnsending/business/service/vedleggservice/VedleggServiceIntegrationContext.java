package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import org.springframework.context.annotation.*;

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

}

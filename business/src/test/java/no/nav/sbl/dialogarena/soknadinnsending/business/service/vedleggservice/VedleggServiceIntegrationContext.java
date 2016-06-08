package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v2.henvendelse.HenvendelsePortType;
import org.springframework.context.annotation.*;

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

}

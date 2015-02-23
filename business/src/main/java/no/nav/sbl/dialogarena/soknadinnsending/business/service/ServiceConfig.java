package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        DefaultVedleggService.class,
        LandService.class,
        SoknadService.class,
        SelfTestService.class,
        StartDatoService.class,
        FaktaService.class
})
public class ServiceConfig {
}

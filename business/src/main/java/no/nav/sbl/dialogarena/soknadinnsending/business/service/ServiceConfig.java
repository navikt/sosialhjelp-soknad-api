package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ConfigService.class,
        DefaultVedleggService.class,
        EosLandService.class,
        SoknadService.class,
        StartDatoService.class,
})
public class ServiceConfig {
}

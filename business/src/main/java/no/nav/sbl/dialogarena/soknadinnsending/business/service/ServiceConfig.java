package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadServiceUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        DefaultVedleggService.class,
        LandService.class,
        SoknadService.class,
        StartDatoService.class,
        FaktaService.class,
        SoknadServiceUtil.class
})
public class ServiceConfig {
}

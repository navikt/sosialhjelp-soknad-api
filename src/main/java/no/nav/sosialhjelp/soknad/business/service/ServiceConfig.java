package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sosialhjelp.soknad.business.service.digisosapi.DigisosApiService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SoknadService.class,
        DigisosApiService.class,
        SoknadMetricsService.class,
        TextService.class,
})
public class ServiceConfig {
}

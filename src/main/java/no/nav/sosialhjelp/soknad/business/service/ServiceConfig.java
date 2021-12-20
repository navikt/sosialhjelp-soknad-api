package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SoknadMetricsService.class,
        HenvendelseService.class,
        TextService.class,
})
public class ServiceConfig {
}

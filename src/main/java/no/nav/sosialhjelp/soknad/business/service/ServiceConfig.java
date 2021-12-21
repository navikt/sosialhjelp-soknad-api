package no.nav.sosialhjelp.soknad.business.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        HenvendelseService.class,
        TextService.class,
})
public class ServiceConfig {
}

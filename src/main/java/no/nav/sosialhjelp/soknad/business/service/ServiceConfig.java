package no.nav.sosialhjelp.soknad.business.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        TextService.class,
})
public class ServiceConfig {
}

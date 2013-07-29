package no.nav.sbl.dialogarena.soknad.config;

import no.nav.sbl.dialogarena.soknad.WicketApplication;
import no.nav.sbl.dialogarena.soknad.service.SoknadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ConsumerConfig.class})
public class ApplicationContext {

    @Bean
    public WicketApplication soknadApplication() {
        return new WicketApplication();
    }

    @Bean
    public SoknadService soknadService() {
        return new SoknadService();
    }
}

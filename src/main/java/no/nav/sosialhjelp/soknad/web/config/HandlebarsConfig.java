package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.business.pdf.HandleBarKjoerer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = HandleBarKjoerer.class)
public class HandlebarsConfig {

    @Bean
    public HandleBarKjoerer handleBarKjoerer() {
        return new HandleBarKjoerer();
    }
}
package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoknadinnsendingLocalConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return Mockito.mock(Tilgangskontroll.class);
    }
}
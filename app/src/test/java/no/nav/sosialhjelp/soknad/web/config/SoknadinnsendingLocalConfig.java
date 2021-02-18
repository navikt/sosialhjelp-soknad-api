package no.nav.sosialhjelp.soknad.web.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;

@Configuration
public class SoknadinnsendingLocalConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return Mockito.mock(Tilgangskontroll.class);
    }
}
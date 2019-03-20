package no.nav.sbl.dialogarena.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;

@Configuration
public class SoknadinnsendingLocalConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return Mockito.mock(Tilgangskontroll.class);
    }
}
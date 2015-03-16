package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SikkerhetsConfig {

    @Bean
    public SikkerhetsAspect sikkerhet() {
        return new SikkerhetsAspect();
    }

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }

}

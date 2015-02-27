package no.nav.sbl.dialogarena.soknadinnsending;


import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.Tilgangskontroll;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;

@Configurable
public class SikkerhetsConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }

}

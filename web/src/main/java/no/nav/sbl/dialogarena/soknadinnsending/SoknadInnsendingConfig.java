package no.nav.sbl.dialogarena.soknadinnsending;


import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.Tilgangskontroll;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Configurable
@Import(SoknadInnsendingConfig.SikkerhetsConfig.class)
@ComponentScan
public class SoknadInnsendingConfig {

    @Configurable
    static class SikkerhetsConfig {
        @Bean
        public Tilgangskontroll tilgangskontroll() {
            return new Tilgangskontroll();
        }
    }

}

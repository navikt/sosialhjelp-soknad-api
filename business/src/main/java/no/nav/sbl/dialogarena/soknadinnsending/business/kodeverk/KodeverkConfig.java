package no.nav.sbl.dialogarena.soknadinnsending.business.kodeverk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KodeverkConfig {

    @Bean
    public Kodeverk kodeverk() {
        return new JsonKodeverk(getClass().getResourceAsStream("/kodeverk.json"));
    }
}
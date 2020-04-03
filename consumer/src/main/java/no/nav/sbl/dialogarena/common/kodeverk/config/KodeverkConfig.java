package no.nav.sbl.dialogarena.common.kodeverk.config;

import no.nav.sbl.dialogarena.common.kodeverk.JsonKodeverk;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KodeverkConfig {
    public KodeverkConfig() {
    }

    @Bean
    public Kodeverk kodeverk() {
        return new JsonKodeverk(this.getClass().getResourceAsStream("/kodeverk.json"));
    }
}
package no.nav.sbl.dialogarena.config;

import no.nav.metrics.aspects.TimerAspect;
import org.springframework.context.annotation.Bean;

public class MetricsConfig {

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }
}

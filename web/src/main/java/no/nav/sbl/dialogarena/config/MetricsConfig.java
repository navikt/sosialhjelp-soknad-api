package no.nav.sbl.dialogarena.config;

import no.nav.metrics.MetricsClient;
import no.nav.metrics.aspects.TimerAspect;
import org.springframework.context.annotation.Bean;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

public class MetricsConfig {

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    public MetricsConfig() {
        String miljo = System.getenv("FASIT_ENVIRONMENT_NAME");
        if (!parseBoolean(getProperty("disable.metrics.report"))) {
            MetricsClient.enableMetrics(no.nav.metrics.MetricsConfig.resolveNaisConfig().withEnvironment(miljo));
        }
    }
}

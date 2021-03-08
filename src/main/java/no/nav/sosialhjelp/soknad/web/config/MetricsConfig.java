package no.nav.sosialhjelp.soknad.web.config;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.DefaultExports;
import no.nav.sosialhjelp.metrics.MetricsClient;
import no.nav.sosialhjelp.metrics.aspects.TimerAspect;
import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

public class MetricsConfig {

    @Value("${sensu_client_host}")
    private String host;

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    public MetricsConfig() {
        String miljo = System.getenv("ENVIRONMENT_NAME");
        if (!parseBoolean(getProperty("disable.metrics.report"))) {
            MetricsClient.enableMetrics(no.nav.sosialhjelp.metrics.MetricsConfig.resolveNaisConfig(MiljoUtils.getNaisAppName(), miljo, host));
        }
    }

    @Bean
    public CollectorRegistry collectorRegistry() {
        DefaultExports.initialize();
        return CollectorRegistry.defaultRegistry;
    }
}

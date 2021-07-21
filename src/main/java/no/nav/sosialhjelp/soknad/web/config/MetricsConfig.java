package no.nav.sosialhjelp.soknad.web.config;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.DefaultExports;
import no.nav.sosialhjelp.metrics.MetricsClient;
import no.nav.sosialhjelp.metrics.aspects.TimerAspect;
import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class MetricsConfig {

    @Value("${sensu_client_host}")
    private String host;

    @Value("${metrics.report.enabled}")
    private boolean metricsReportEnabled;

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    public MetricsConfig() {
        String miljo = System.getenv("ENVIRONMENT_NAME");
        if (metricsReportEnabled) {
            MetricsClient.enableMetrics(no.nav.sosialhjelp.metrics.MetricsConfig.resolveNaisConfig(MiljoUtils.getNaisAppName(), miljo, host));
        }
    }

    @Bean
    public CollectorRegistry collectorRegistry() {
        DefaultExports.initialize();
        return CollectorRegistry.defaultRegistry;
    }
}

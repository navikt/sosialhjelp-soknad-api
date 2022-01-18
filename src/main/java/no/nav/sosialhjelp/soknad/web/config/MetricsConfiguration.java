package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.metrics.MetricsClient;
import no.nav.sosialhjelp.metrics.MetricsConfig;
import no.nav.sosialhjelp.metrics.aspects.TimerAspect;
import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class MetricsConfiguration {

    @Value("${sensu_client_host}")
    private String host;

    @Value("${metrics.report.enabled}")
    private boolean metricsReportEnabled;

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public MetricProperties metricProperties() {
        var miljo = System.getenv("ENVIRONMENT_NAME");
        var metricProperties = new MetricProperties(host, miljo);
        if (metricsReportEnabled) {
            metricProperties.enableMetrics();
        }
        return metricProperties;
    }

    static class MetricProperties {

        private final String host;
        private final String miljo;

        public MetricProperties(String host, String miljo) {
            this.host = host;
            this.miljo = miljo;
        }

        public void enableMetrics() {
            MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig(MiljoUtils.getNaisAppName(), miljo, host));
        }
    }
}

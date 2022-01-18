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

    @Value("${sensu_client_port}")
    private String port;

    @Value("${metrics.report.enabled}")
    private boolean metricsReportEnabled;

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public MetricProperties metricProperties() {
        var miljo = System.getenv("ENVIRONMENT_NAME");
        var metricProperties = new MetricProperties(host, miljo, port);
        if (metricsReportEnabled) {
            metricProperties.enableMetrics();
        }
        return metricProperties;
    }

//    @Bean
//    public CollectorRegistry collectorRegistry() {
//        DefaultExports.initialize();
//        return CollectorRegistry.defaultRegistry;
//    }

    static class MetricProperties {

        private final String host;
        private final String miljo;
        private final String port;

        public MetricProperties(String host, String miljo, String port) {
            this.host = host;
            this.miljo = miljo;
            this.port = port;
        }

        public void enableMetrics() {
            System.setProperty("sensu_client_port", port);
            MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig(MiljoUtils.getNaisAppName(), miljo, host));
        }
    }
}

package no.nav.sosialhjelp.soknad.metrics

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.MetricsServlet
import io.prometheus.client.hotspot.DefaultExports
import no.nav.sosialhjelp.metrics.MetricsClient
import no.nav.sosialhjelp.metrics.MetricsConfig.resolveNaisConfig
import no.nav.sosialhjelp.metrics.aspects.TimerAspect
import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MetricsConfig(
    @Value("\${sensu_client_host}") private val host: String?,
    @Value("\${metrics.report.enabled}") private val metricsReportEnabled: Boolean
) {

    @Bean
    open fun timerAspect(): TimerAspect? {
        return TimerAspect()
    }

    @Bean
    open fun soknadMetricsService(): SoknadMetricsService {
        return SoknadMetricsService()
    }

    @Bean
    open fun collectorRegistry(): CollectorRegistry? {
        DefaultExports.initialize()
        return CollectorRegistry.defaultRegistry
    }

    @Bean
    open fun metricsServlet(): ServletRegistrationBean<*> {
        val metricsServlet = ServletRegistrationBean<MetricsServlet>()
        metricsServlet.servlet = MetricsServlet()
        metricsServlet.urlMappings = listOf("/internal/metrics/*")
        return metricsServlet
    }

    @Bean
    open fun metricProperties(): MetricProperties? {
        val miljo = System.getenv("ENVIRONMENT_NAME")
        val metricProperties = MetricProperties(host, miljo)
        if (metricsReportEnabled) {
            metricProperties.enableMetrics()
        }
        return metricProperties
    }

    class MetricProperties(
        private val host: String?,
        private val miljo: String?
    ) {
        fun enableMetrics() {
            MetricsClient.enableMetrics(resolveNaisConfig(MiljoUtils.getNaisAppName(), miljo, host))
        }
    }
}

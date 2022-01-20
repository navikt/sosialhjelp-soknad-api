package no.nav.sosialhjelp.soknad.metrics

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.MetricsServlet
import io.prometheus.client.hotspot.DefaultExports
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MetricsConfig {

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
}

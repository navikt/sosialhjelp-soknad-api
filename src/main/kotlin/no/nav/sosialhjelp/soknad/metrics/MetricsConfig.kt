package no.nav.sosialhjelp.soknad.metrics

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MetricsConfig {

    @Bean
    open fun soknadMetricsService(): SoknadMetricsService {
        return SoknadMetricsService()
    }
}

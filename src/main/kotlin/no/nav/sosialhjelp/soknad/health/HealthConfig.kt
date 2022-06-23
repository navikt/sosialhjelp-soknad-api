package no.nav.sosialhjelp.soknad.health

import io.micrometer.core.instrument.MeterRegistry
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.SelftestService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class HealthConfig(
    @Value("\${application.version}") private val applicationVersion: String,
    private val dependencyChecks: List<DependencyCheck>,
    private val meterRegistry: MeterRegistry
) {

    @Bean
    open fun selftestService(): SelftestService {
        return SelftestService(
            appName = "sosialhjelp-soknad-api",
            version = applicationVersion,
            dependencyChecks = dependencyChecks,
            meterRegistry = meterRegistry
        )
    }
}

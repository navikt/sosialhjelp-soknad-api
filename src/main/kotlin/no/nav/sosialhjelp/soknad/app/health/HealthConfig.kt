package no.nav.sosialhjelp.soknad.app.health

import io.micrometer.core.instrument.MeterRegistry
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.SelftestService
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class HealthConfig(
    private val dependencyChecks: List<DependencyCheck>,
    private val meterRegistry: MeterRegistry
) {

    @Bean
    open fun selftestService(): SelftestService {
        return SelftestService(
            appName = "sosialhjelp-soknad-api",
            version = MiljoUtils.appImageVersion,
            dependencyChecks = dependencyChecks,
            meterRegistry = meterRegistry
        )
    }
}

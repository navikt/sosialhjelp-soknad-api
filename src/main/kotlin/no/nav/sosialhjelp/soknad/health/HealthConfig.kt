package no.nav.sosialhjelp.soknad.health

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.SelftestMeterBinder
import no.nav.sosialhjelp.selftest.SelftestService
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class HealthConfig(
    private val dependencyChecks: List<DependencyCheck>
) {
    @Bean
    open fun selftestService(): SelftestService {
        return SelftestService("sosialhjelp-soknad-api", MiljoUtils.getAppImage(), dependencyChecks)
    }

    @Bean
    open fun selftestMeterBinder(selftestService: SelftestService): SelftestMeterBinder {
        return SelftestMeterBinder(selftestService)
    }
}

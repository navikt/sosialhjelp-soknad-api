package no.nav.sosialhjelp.soknad.app.config

import no.nav.sosialhjelp.soknad.app.soknadlock.ConflictAvoidanceDelayInterceptor
import no.nav.sosialhjelp.soknad.v2.accessinterceptor.SoknadAccessInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Profile("!no-interceptor")
class WebConfig(
    private val conflictAvoidanceDelayInterceptor: ConflictAvoidanceDelayInterceptor,
    private val soknadAccessInterceptor: SoknadAccessInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(conflictAvoidanceDelayInterceptor)
        registry.addInterceptor(soknadAccessInterceptor)
    }
}

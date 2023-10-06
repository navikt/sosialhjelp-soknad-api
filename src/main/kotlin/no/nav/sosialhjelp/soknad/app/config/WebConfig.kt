package no.nav.sosialhjelp.soknad.app.config

import no.nav.sosialhjelp.soknad.app.interceptor.ConflictAvoidanceDelayInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val conflictAvoidanceDelayInterceptor: ConflictAvoidanceDelayInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(conflictAvoidanceDelayInterceptor)
    }
}

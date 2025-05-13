package no.nav.sosialhjelp.soknad.app.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Profile("!no-interceptor)")
class WebConfig(
    private val soknadApiInterceptors: List<SoknadApiHandlerInterceptor>,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        soknadApiInterceptors.forEach { registry.addInterceptor(it) }
    }
}

interface SoknadApiHandlerInterceptor : HandlerInterceptor

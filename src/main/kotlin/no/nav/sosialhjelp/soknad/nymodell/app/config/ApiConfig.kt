package no.nav.sosialhjelp.soknad.nymodell.app.config

import no.nav.sosialhjelp.soknad.nymodell.domene.soknad.SoknadRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Profile("!no-interceptor")
@Configuration
class ApiConfig(
    val soknadRepository: SoknadRepository
): WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SoknadEierInterceptor(soknadRepository))
            .excludePathPatterns("/soknad/opprettSoknad")
    }
}
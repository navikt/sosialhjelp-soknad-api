package no.nav.sosialhjelp.soknad.app.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.LocalDateTime
import java.time.Month

@Profile("!test")
@Configuration
class StengSoknadConfig: WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(StengSoknadInterceptor)
            .addPathPatterns("/soknader/{behandlingsId}/actions/send")
            .addPathPatterns("/soknader/opprettSoknad")
    }

    companion object StengSoknadInterceptor : HandlerInterceptor {

        override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
            val nedePeriodeStart = LocalDateTime.of(2024, Month.APRIL, 9, 9, 0)

            with(LocalDateTime.now()) {
                if (isAfter(nedePeriodeStart)) {
                    throw TjenesteUtilgjengeligException("Søknaden er midlertidig stengt.", null)
                }
            }
            return true
        }
    }
}

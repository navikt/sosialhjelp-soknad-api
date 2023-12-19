package no.nav.sosialhjelp.soknad.app

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.LocalDateTime
import java.time.Month

@Configuration
class StengSoknadConfig : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(StengSoknadInterceptor)
    }

    companion object StengSoknadInterceptor : HandlerInterceptor {
        override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
//            val nedePeriodeStart = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0)
//            val nedePeriodeSlutt = LocalDateTime.of(2024, Month.JANUARY, 2, 8, 0)

            val nedePeriodeStart = LocalDateTime.of(2023, Month.DECEMBER, 19, 0, 0)
            val nedePeriodeSlutt = LocalDateTime.of(2023, Month.DECEMBER, 20, 8, 0)

            with(LocalDateTime.now()) {
                if (isAfter(nedePeriodeStart) && isBefore(nedePeriodeSlutt)) {
                    throw TjenesteUtilgjengeligException("Søknaden er midlertidig stengt.", null)
                }
            }

            return true
        }
    }
}

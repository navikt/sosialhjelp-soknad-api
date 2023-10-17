package no.nav.sosialhjelp.soknad.app.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class NoContentFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        filterChain.doFilter(request, response)
        // TODO - skal denne sette status = NO_CONTENT uansett hva slags responsekode som ligger inne?
        if (response.status == HttpStatus.OK.value()) {
            if (response.contentType == null || response.contentType.equals("")) {
                response.status = HttpStatus.NO_CONTENT.value()
            }
        }
    }
}

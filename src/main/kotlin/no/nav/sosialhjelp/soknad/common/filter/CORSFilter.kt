package no.nav.sosialhjelp.soknad.common.filter

import no.nav.sosialhjelp.soknad.common.ServiceUtils
import org.springframework.stereotype.Component
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

@Provider
@Component
class CORSFilter(
    private val serviceUtils: ServiceUtils
) : ContainerResponseFilter {

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        val origin = requestContext.getHeaderString("Origin") ?: "*"
        if (serviceUtils.isNonProduction() || ALLOWED_ORIGINS.contains(origin)) {
            responseContext.headers.add("Access-Control-Allow-Origin", origin)
            responseContext.headers.add(
                "Access-Control-Allow-Headers",
                "Origin, Content-Type, Accept, X-XSRF-TOKEN, Nav-Call-Id, Authorization, sentry-trace"
            )
            responseContext.headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            responseContext.headers.add("Access-Control-Allow-Credentials", "true")
        }
    }

    companion object {
        private val ALLOWED_ORIGINS = listOf(
            "https://tjenester.nav.no",
            "https://www.nav.no"
        )
    }
}

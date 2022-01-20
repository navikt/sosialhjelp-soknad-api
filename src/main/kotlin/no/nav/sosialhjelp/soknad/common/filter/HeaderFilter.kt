package no.nav.sosialhjelp.soknad.common.filter

import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

@Provider
class HeaderFilter : ContainerResponseFilter {

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        responseContext.headers.add("X-Content-Type-Options", "nosniff")
        responseContext.headers.add("X-XSS-Protection", "1; mode=block")
        responseContext.headers.add("Cache-Control", "private, max-age=0, no-cache, no-store")
    }
}

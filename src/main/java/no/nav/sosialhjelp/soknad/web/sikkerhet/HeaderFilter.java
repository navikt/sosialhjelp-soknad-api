package no.nav.sosialhjelp.soknad.web.sikkerhet;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class HeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("X-Content-Type-Options", "nosniff");
        responseContext.getHeaders().add("X-XSS-Protection", "1; mode=block");
        responseContext.getHeaders().add("Cache-Control", "private, max-age=0, no-cache, no-store");
    }
}

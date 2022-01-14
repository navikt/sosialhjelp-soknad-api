package no.nav.sosialhjelp.soknad.web.sikkerhet;

import no.nav.sosialhjelp.soknad.common.ServiceUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

@Provider
@Component
public class CORSFilter implements ContainerResponseFilter {
    private static final List<String> ALLOWED_ORIGINS = asList(
            "https://tjenester.nav.no",
            "https://www.nav.no");

    private final ServiceUtils serviceUtils;

    public CORSFilter(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        var origin = Optional.ofNullable(requestContext.getHeaderString("Origin")).orElse("*");
        if (serviceUtils.isNonProduction() || ALLOWED_ORIGINS.contains(origin)) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-XSRF-TOKEN, Nav-Call-Id, Authorization, sentry-trace");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        }
    }
}

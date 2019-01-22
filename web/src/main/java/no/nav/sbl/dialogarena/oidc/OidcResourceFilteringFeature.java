package no.nav.sbl.dialogarena.oidc;

import no.nav.security.oidc.jaxrs.OidcContainerRequestFilter;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.util.Arrays;
import java.util.List;

public class OidcResourceFilteringFeature implements DynamicFeature {
    private static final List<Class> WHITELISTED_CLASSES = Arrays.asList();

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if(WHITELISTED_CLASSES.contains(resourceInfo.getResourceClass()) || isOidcMock()) {
            return;
        }
        context.register(OidcContainerRequestFilter.class);
    }

    private boolean isOidcMock() {
        return "true".equalsIgnoreCase(System.getProperty("start.oidc.withmock")) && 
                "true".equalsIgnoreCase(System.getProperty("start.oidc.withmock"));
    }
}

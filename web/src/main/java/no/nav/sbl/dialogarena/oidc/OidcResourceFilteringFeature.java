package no.nav.sbl.dialogarena.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.security.oidc.jaxrs.OidcContainerRequestFilter;
import org.glassfish.jersey.server.wadl.processor.OptionsMethodProcessor;
import org.glassfish.jersey.server.wadl.processor.WadlModelProcessor;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.util.Collections;
import java.util.List;

public class OidcResourceFilteringFeature implements DynamicFeature {
    private static final List<Class> WHITELISTED_CLASSES = Collections.singletonList(WadlModelProcessor.OptionsHandler.class); // Add Resource-classes from external libraries we need to use but can't annotate with @unprotected.
    private static final List<Class> WHITELISTED_PARENT_CLASSES = Collections.singletonList(OptionsMethodProcessor.class);

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if( !OidcFeatureToggleUtils.isRunningWithOidc() || isWhitelistedInProd(resourceInfo) || isWhitelistedWhenNotRunningInProd()) {
            return;
        }
        context.register(OidcContainerRequestFilter.class);
    }

    private boolean isWhitelistedInProd(ResourceInfo resourceInfo) {
        return WHITELISTED_CLASSES.contains(resourceInfo.getResourceClass())
                || WHITELISTED_PARENT_CLASSES.contains(resourceInfo.getResourceClass().getEnclosingClass());
    }

    private boolean isWhitelistedWhenNotRunningInProd() {
        return !ServiceUtils.isRunningInProd() && (isOidcMock() || MockUtils.isTillatMockRessurs());
    }

    private boolean isOidcMock() {
        return "true".equalsIgnoreCase(System.getProperty("tillatmock")) &&
                "true".equalsIgnoreCase(System.getProperty("start.oidc.withmock"));
    }
}

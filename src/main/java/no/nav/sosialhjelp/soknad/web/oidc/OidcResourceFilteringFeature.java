//package no.nav.sosialhjelp.soknad.web.oidc;
//
//import no.nav.security.token.support.jaxrs.JwtTokenContainerRequestFilter;
//import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
//import org.glassfish.jersey.server.wadl.processor.OptionsMethodProcessor;
//import org.glassfish.jersey.server.wadl.processor.WadlModelProcessor;
//
//import javax.ws.rs.container.DynamicFeature;
//import javax.ws.rs.container.ResourceInfo;
//import javax.ws.rs.core.FeatureContext;
//import java.util.Collections;
//import java.util.List;
//
//@SuppressWarnings("rawtypes")
//public class OidcResourceFilteringFeature implements DynamicFeature {
//    private static final List<Class> ALLOWED_CLASSES = Collections.singletonList(WadlModelProcessor.OptionsHandler.class); // Add Resource-classes from external libraries we need to use but can't annotate with @unprotected.
//    private static final List<Class> ALLOWED_PARENT_CLASSES = Collections.singletonList(OptionsMethodProcessor.class);
//
//    @Override
//    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
//        if(isClassAllowedInProd(resourceInfo) || isAllowedWhenNotRunningInProd()) {
//            return;
//        }
//        context.register(JwtTokenContainerRequestFilter.class);
//    }
//
//    private boolean isClassAllowedInProd(ResourceInfo resourceInfo) {
//        return ALLOWED_CLASSES.contains(resourceInfo.getResourceClass())
//                || ALLOWED_PARENT_CLASSES.contains(resourceInfo.getResourceClass().getEnclosingClass());
//    }
//
//    private boolean isAllowedWhenNotRunningInProd() {
//        return ServiceUtils.isNonProduction() && (isOidcMock());
//    }
//
//    private boolean isOidcMock() {
//        return "true".equalsIgnoreCase(System.getProperty("tillatmock")) &&
//                "true".equalsIgnoreCase(System.getProperty("start.oidc.withmock"));
//    }
//}

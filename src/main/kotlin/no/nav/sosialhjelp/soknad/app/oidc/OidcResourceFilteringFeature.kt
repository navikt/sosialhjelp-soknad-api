package no.nav.sosialhjelp.soknad.app.oidc

import no.nav.security.token.support.jaxrs.JwtTokenContainerRequestFilter
import org.glassfish.jersey.server.wadl.processor.OptionsMethodProcessor
import org.glassfish.jersey.server.wadl.processor.WadlModelProcessor.OptionsHandler
import org.springframework.boot.actuate.endpoint.web.jersey.JerseyEndpointResourceFactory
import org.springframework.stereotype.Component
import javax.ws.rs.container.DynamicFeature
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.FeatureContext

@Component
class OidcResourceFilteringFeature : DynamicFeature {

    override fun configure(resourceInfo: ResourceInfo, context: FeatureContext) {
        if (isClassAllowedInProd(resourceInfo)) {
            return
        }
        context.register(JwtTokenContainerRequestFilter::class.java)
    }

    private fun isClassAllowedInProd(resourceInfo: ResourceInfo): Boolean {
        return ALLOWED_CLASSES.contains(resourceInfo.resourceClass) || ALLOWED_PARENT_CLASSES.contains(resourceInfo.resourceClass.enclosingClass)
    }

    companion object {
        private val ALLOWED_CLASSES = listOf<Class<*>>(OptionsHandler::class.java) // Add Resource-classes from external libraries we need to use but can't annotate with @unprotected.
        private val ALLOWED_PARENT_CLASSES = listOf<Class<*>>(OptionsMethodProcessor::class.java, JerseyEndpointResourceFactory::class.java)
    }
}

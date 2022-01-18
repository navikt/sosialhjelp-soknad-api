package no.nav.sosialhjelp.soknad.common.oidc

import no.nav.security.token.support.jaxrs.JwtTokenContainerRequestFilter
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.ws.rs.container.DynamicFeature
import javax.ws.rs.container.ResourceInfo
import javax.ws.rs.core.FeatureContext

@Component
class OidcResourceFilteringFeature(
    @Value("\${tillatmock}") private val tillatmock: String,
    @Value("\${start.oidc.withmock}") private val startOidcMock: String
) : DynamicFeature {

    override fun configure(resourceInfo: ResourceInfo, context: FeatureContext) {
        if (isAllowedWhenNotRunningInProd) {
            return
        }
        context.register(JwtTokenContainerRequestFilter::class.java)
    }

    private val isAllowedWhenNotRunningInProd: Boolean
        get() = ServiceUtils.isNonProduction() && isOidcMock

    private val isOidcMock: Boolean
        get() = "true".equals(tillatmock, ignoreCase = true) &&
            "true".equals(startOidcMock, ignoreCase = true)
}

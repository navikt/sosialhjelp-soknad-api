package no.nav.sosialhjelp.soknad.app.security.principal

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.BearerTokenErrors

class CitizenPrincipal(
    token: String,
    attributes: Map<String, Any>,
    authorities: Collection<GrantedAuthority>,
) : OAuth2AuthenticatedPrincipal by citizenDelegate(attributes, authorities) {
    val token: String = requireBearerToken(token)
    val userId: String get() = getName()

    override fun <A : Any> getAttribute(name: String): A? {
        return super.getAttribute(name)
    }
}

private fun citizenDelegate(
    attributes: Map<String, Any>,
    authorities: Collection<GrantedAuthority>,
): OAuth2AuthenticatedPrincipal {
    val userId =
        (attributes["pid"] as? String)?.takeIf { it.isNotBlank() }
            ?: (attributes["sub"] as? String)?.takeIf { it.isNotBlank() }
            ?: throw OAuth2AuthenticationException(
                BearerTokenErrors.invalidToken("Missing user identity in pid or sub claim"),
            )

    return DefaultOAuth2AuthenticatedPrincipal(userId, attributes, authorities)
}

internal fun requireBearerToken(token: String): String =
    token.takeIf { it.isNotBlank() }
        ?: throw OAuth2AuthenticationException(
            BearerTokenErrors.invalidToken("Missing token value"),
        )

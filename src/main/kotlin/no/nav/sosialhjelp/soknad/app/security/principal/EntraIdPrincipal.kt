package no.nav.sosialhjelp.soknad.app.security.principal

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

class EntraIdPrincipal(
    token: String,
    attributes: Map<String, Any>,
    authorities: Collection<GrantedAuthority>,
) : OAuth2AuthenticatedPrincipal by DefaultOAuth2AuthenticatedPrincipal(attributes, authorities) {
    val token: String = requireBearerToken(token)
}

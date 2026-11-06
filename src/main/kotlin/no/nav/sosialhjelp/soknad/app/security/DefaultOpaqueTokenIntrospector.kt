package no.nav.sosialhjelp.soknad.app.security

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector

class DefaultOpaqueTokenIntrospector(private val identityProvider: IdentityProvider, private val texasService: TexasService) : OpaqueTokenIntrospector {
    override fun introspect(token: String): OAuth2AuthenticatedPrincipal =
        runCatching {
            val validationResult = texasService.validateToken(identityProvider, token)
            if (!validationResult.active) {
                throw BadOpaqueTokenException("Token is not active for identity provider ${validationResult.identityProvider}")
            }
            DefaultOAuth2AuthenticatedPrincipal(
                validationResult.claims,
                addAuthorities(identityProvider, validationResult.claims),
            )
        }
            // TODO Bedre feilhåndtering - kun for test
            .getOrElse {
                logger.error("Noe feilet i token-validering: ${it.message}", it)
                throw it
            }

//            .getOrElse {
//                when(it) {
//                    is BadOpaqueTokenException -> throw it
//                    is WebClientRequestException, WebClientResponseException, DecodingException,
//                    else -> throw OAuth2IntrospectionException("Failed to introspect token for identity provider ${identityProvider.value}", it)
//                }
// }

    private fun addAuthorities(
        identityProvider: IdentityProvider,
        claims: Map<String, Any>,
    ): List<SimpleGrantedAuthority> {
        return when (identityProvider) {
            IdentityProvider.IDPORTEN, IdentityProvider.TOKENX ->
                (claims["acr"] as? String)
                    ?.takeIf { it in supportedAcrValues }
                    ?.let { listOf(SimpleGrantedAuthority(it)) }
                    ?: emptyList()
            else -> emptyList()
        }
    }

    companion object {
        private val logger by logger()

        private val supportedAcrValues =
            setOf(
                "Level3",
                "idporten-loa-substantial",
                "Level4",
                "idporten-loa-high",
            )
    }
}

data class ValidationResult(
    val identityProvider: IdentityProvider,
    val active: Boolean,
    val claims: Map<String, Any>,
)

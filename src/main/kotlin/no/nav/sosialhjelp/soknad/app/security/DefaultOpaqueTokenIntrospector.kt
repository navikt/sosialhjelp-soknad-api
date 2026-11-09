package no.nav.sosialhjelp.soknad.app.security

import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasService
import org.springframework.core.codec.DecodingException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Instant

class DefaultOpaqueTokenIntrospector(private val identityProvider: IdentityProvider, private val texasService: TexasService) : OpaqueTokenIntrospector {
    override fun introspect(token: String): OAuth2AuthenticatedPrincipal =
        runCatching {
            val validationResult = texasService.validateToken(identityProvider, token)
            if (!validationResult.active) {
                throw BadOpaqueTokenException("Token is not active for identity provider ${validationResult.identityProvider}")
            }
            DefaultOAuth2AuthenticatedPrincipal(
                validationResult.claims.addClaims(),
                addAuthorities(identityProvider, validationResult.claims),
            )
        }
            .getOrElse {
                when (it) {
                    is BadOpaqueTokenException -> throw it
                    is WebClientRequestException, is WebClientResponseException, is DecodingException ->
                        throw OAuth2IntrospectionException("Failed to introspect token for identity provider ${identityProvider.value}", it)
                    else -> throw OAuth2IntrospectionException("Failed to introspect token for identity provider ${identityProvider.value}", it)
                }
            }

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
        private val supportedAcrValues =
            setOf(
                "Level3",
                "idporten-loa-substantial",
                "Level4",
                "idporten-loa-high",
            )
    }
}

private fun Map<String, Any>.addClaims(): MutableMap<String, Any> =
    this.mapValues { (name, value) ->
        when (name in setOf("iat", "exp", "nbf") && value is Number) {
            true -> Instant.ofEpochSecond(value.toLong())
            false -> value
        }
    }.toMutableMap()

data class ValidationResult(
    val identityProvider: IdentityProvider,
    val active: Boolean,
    val claims: Map<String, Any>,
)

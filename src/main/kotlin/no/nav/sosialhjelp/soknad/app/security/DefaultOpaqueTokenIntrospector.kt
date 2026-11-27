package no.nav.sosialhjelp.soknad.app.security

import no.nav.sosialhjelp.soknad.app.security.principal.CitizenPrincipal
import no.nav.sosialhjelp.soknad.app.security.principal.EntraIdPrincipal
import no.nav.sosialhjelp.soknad.app.security.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.app.security.texas.TexasService
import org.springframework.core.codec.DecodingException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Instant

class DefaultOpaqueTokenIntrospector(private val identityProvider: IdentityProvider, private val texasService: TexasService) : OpaqueTokenIntrospector {
    override fun introspect(notValidatedToken: String): OAuth2AuthenticatedPrincipal =
        runCatching {
            validateToken(notValidatedToken)
                .let { validatedResult ->
                    val claims = validatedResult.claims.addClaims()
                    val authorities = addAuthorities(validatedResult.identityProvider, claims)
                    returnPrincipal(validatedResult.token, claims, authorities)
                }
        }
            .getOrElse {
                when (it) {
                    is BadOpaqueTokenException -> throw it
                    is WebClientRequestException, is WebClientResponseException, is DecodingException ->
                        throw OAuth2IntrospectionException("Failed to introspect token for identity provider ${identityProvider.value}", it)
                    else -> throw OAuth2IntrospectionException("Failed to introspect token for identity provider ${identityProvider.value}", it)
                }
            }

    private fun validateToken(token: String): ValidationResult {
        val validationResult = texasService.validateToken(identityProvider, token)
        if (!validationResult.active) {
            throw BadOpaqueTokenException("Token is not active for identity provider ${validationResult.identityProvider}")
        }
        return validationResult
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

    private fun returnPrincipal(
        token: String,
        attributes: Map<String, Any>,
        authorities: List<SimpleGrantedAuthority>,
    ): OAuth2AuthenticatedPrincipal =
        when (identityProvider) {
            IdentityProvider.IDPORTEN, IdentityProvider.TOKENX -> createCitizenPrincipal(token, attributes, authorities)
            else -> EntraIdPrincipal(token, attributes, authorities)
        }

    private fun createCitizenPrincipal(
        token: String,
        attributes: Map<String, Any>,
        authorities: List<SimpleGrantedAuthority>,
    ): OAuth2AuthenticatedPrincipal =
        try {
            CitizenPrincipal(token, attributes, authorities)
        } catch (e: OAuth2AuthenticationException) {
            throw BadOpaqueTokenException("Invalid citizen token", e)
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
    val token: String,
    val claims: Map<String, Any>,
)

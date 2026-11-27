package no.nav.sosialhjelp.soknad.app.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class JwtDecoderConfig(
    @param:Value("\${idporten.issuer}") private val issuer: String,
    @param:Value("\${idporten.audience}") private val audience: String,
) {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer) as NimbusJwtDecoder

        DelegatingOAuth2TokenValidator(
            JwtValidators.createDefaultWithIssuer(issuer),
            AudienceValidator(audience),
            AcrValidator(),
        )
            .also { jwtDecoder.setJwtValidator(it) }

        return jwtDecoder
    }
}

class AudienceValidator(
    private val audience: String,
) : OAuth2TokenValidator<Jwt> {
    val error: OAuth2Error = OAuth2Error("invalid_token", "The required audience is missing", null)

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult =
        if (jwt.claims["client_id"] == audience) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(error)
        }
}

class AcrValidator : OAuth2TokenValidator<Jwt> {
    fun error(actualAcr: String): OAuth2Error =
        OAuth2Error("invalid_token", "Wrong acr. Expected one of [Level4, idporten-loa-high], but was $actualAcr", null)

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val acr = jwt.getClaimAsString("acr")
        return if (acr in listOf("Level4", "idporten-loa-high")) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(error(acr ?: "invalid_acr"))
        }
    }
}

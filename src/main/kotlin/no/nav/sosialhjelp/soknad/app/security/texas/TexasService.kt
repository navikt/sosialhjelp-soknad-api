package no.nav.sosialhjelp.soknad.app.security.texas

import no.nav.sosialhjelp.soknad.app.security.ValidationResult
import org.springframework.stereotype.Service
import kotlin.collections.component1

interface TexasService {
    fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String

    fun exchangeToken(
        userToken: String,
        idProvider: IdentityProvider,
        target: String,
    ): String

    fun validateToken(
        idProvider: IdentityProvider,
        token: String,
    ): ValidationResult
}

@Service
class TexasServiceImpl(
    private val texasClient: TexasClient,
) : TexasService {
    override fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String =
        when (val tokenResponse = texasClient.getToken(idProvider.value, target)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error -> error("Failed to fetch token from Texas: $tokenResponse")
            else -> error("Ugyldig response-type: ${tokenResponse::class.simpleName}")
        }

    override fun exchangeToken(
        userToken: String,
        idProvider: IdentityProvider,
        target: String,
    ): String =
        when (val tokenResponse = texasClient.exchangeToken(idProvider.value, target, userToken)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error -> error("Failed to exchange token from Texas: $tokenResponse")
            else -> error("Ugyldig response-type: ${tokenResponse::class.simpleName}")
        }

    override fun validateToken(
        idProvider: IdentityProvider,
        token: String,
    ): ValidationResult =
        when (val tokenResponse = texasClient.validateToken(idProvider.value, token)) {
            is TokenResponse.ValidationResponse ->
                ValidationResult(
                    token = token,
                    active = tokenResponse.active,
                    identityProvider = idProvider,
                    claims = tokenResponse.claims.toMapNotNullValues(),
                )
            is TokenResponse.Error -> error("Failed to validate token from Texas: $tokenResponse")
            else -> error("Ugyldig response-type: ${tokenResponse::class.simpleName}")
        }
}

private fun MutableMap<String, Any?>.toMapNotNullValues(): Map<String, Any> = mapNotNull { (key, value) -> value?.let { v -> key to v } }.toMap()

interface NonBlockingTexasService {
    suspend fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String

    suspend fun exchangeToken(
        userToken: String,
        idProvider: IdentityProvider,
        target: String,
    ): String
}

@Service
class NonBlockingTexasServiceImpl(
    private val texasClient: NonBlockingTexasClient,
) : NonBlockingTexasService {
    override suspend fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String =
        when (val tokenResponse = texasClient.getToken(idProvider.value, target)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error -> error("Failed to fetch token from Texas: $tokenResponse")
            else -> error("Ugyldig response-type: ${tokenResponse::class.simpleName}")
        }

    override suspend fun exchangeToken(
        userToken: String,
        idProvider: IdentityProvider,
        target: String,
    ): String =
        when (val tokenResponse = texasClient.exchangeToken(idProvider.value, target, userToken)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error -> error("Failed to exchange token from Texas: $tokenResponse")
            else -> error("Ugyldig response-type: ${tokenResponse::class.simpleName}")
        }
}

enum class IdentityProvider(val value: String) {
    ENTRA_ID("entra_id"),
    M2M("maskinporten"),
    TOKENX("tokenx"),
    IDPORTEN("idporten"),
}

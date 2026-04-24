package no.nav.sosialhjelp.soknad.auth.texas

import org.springframework.stereotype.Service

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
            is TokenResponse.Error ->
                throw IllegalStateException("Failed to fetch token from Texas: $tokenResponse")
        }

    override fun exchangeToken(
        userToken: String,
        idProvider: IdentityProvider,
        target: String,
    ): String =
        when (val tokenResponse = texasClient.exchangeToken(idProvider.value, target, userToken)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error -> throw IllegalStateException("Failed to exchange token from Texas: $tokenResponse")
        }
}

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
            is TokenResponse.Error ->
                throw IllegalStateException("Failed to fetch token from Texas: $tokenResponse")
        }

    override suspend fun exchangeToken(
        userToken: String,
        idProvider: IdentityProvider,
        target: String,
    ): String =
        when (val tokenResponse = texasClient.exchangeToken(idProvider.value, target, userToken)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error -> throw IllegalStateException("Failed to exchange token from Texas: $tokenResponse")
        }
}

enum class IdentityProvider(val value: String) {
    AZURE_AD("azuread"),
    M2M("maskinporten"),
    TOKENX("tokenx"),
}

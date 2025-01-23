package no.nav.sosialhjelp.soknad.auth.texas

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getTokenOrNull as userTokenOrNull

interface TexasService {
    fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String

    fun exchangeToken(
        idProvider: IdentityProvider,
        target: String,
    ): String
}

class TexasServiceImpl(
    private val texasClient: TexasClient,
) : TexasService {
    override fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String {
        return when (val tokenResponse = texasClient.getToken(idProvider.value, target)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error ->
                throw IllegalStateException("Failed to fetch token from Texas: $tokenResponse")
        }
    }

    override fun exchangeToken(
        idProvider: IdentityProvider,
        target: String,
    ): String {
        return userTokenOrNull()?.let { userToken ->
            when (val tokenResponse = texasClient.exchangeToken(IdentityProvider.AZURE_AD.value, target, userToken)) {
                is TokenResponse.Success -> tokenResponse.token
                is TokenResponse.Error ->
                    throw IllegalStateException("Failed to exchange token from Texas: $tokenResponse")
            }
        } ?: throw IllegalStateException("User token not found")
    }

    companion object {
        private val logger by logger()
    }
}

enum class IdentityProvider(val value: String) {
    AZURE_AD("azuread"),
    M2M("maskinporten"),
    TOKENX("tokenx"),
}

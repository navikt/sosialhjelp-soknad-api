package no.nav.sosialhjelp.soknad.auth.texas

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.springframework.stereotype.Service

interface TexasService {
    fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String

    fun exchangeToken(
        idProvider: IdentityProvider,
        target: String,
        userToken: String,
    ): String
}

@Service
class TexasServiceImpl(
    private val texasClient: TexasClient,
    private val fssHandler: TempProdFssHandler,
) : TexasService {
    override fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String {
        // TODO Midlertidig handler mens prod er i FSS
        return if (MiljoUtils.isProdFss()) {
            fssHandler.handleProdFss(idProvider, target)
        } else {
            when (val tokenResponse = texasClient.getToken(IdentityProvider.AZURE_AD.value, target)) {
                is TokenResponse.Success -> tokenResponse.token
                is TokenResponse.Error ->
                    throw IllegalStateException("Failed to fetch token from Texas: $tokenResponse")
            }
        }
    }

    // TODO Kan vi benytte SubjectHandlerUtils for user token ?
    override fun exchangeToken(
        idProvider: IdentityProvider,
        target: String,
        userToken: String,
    ): String {
        return when (val tokenResponse = texasClient.exchangeToken(IdentityProvider.AZURE_AD.value, target, userToken)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error ->
                throw IllegalStateException("Failed to exchange token from Texas: $tokenResponse")
        }
    }
}

enum class IdentityProvider(val value: String) {
    AZURE_AD("azuread"),
    M2M("maskinporten"),
    TOKENX("tokenx"),
}

package no.nav.sosialhjelp.soknad.auth.texas

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import org.springframework.stereotype.Service

interface TexasService {
    fun getAzureAdToken(target: String): String
}

@Service
class TexasServiceImpl(
    private val texasClient: TexasClient,
    private val azureadService: AzureadService,
) : TexasService {
    override fun getAzureAdToken(target: String): String {
        // TODO Midlertidig til FSS er borte
        return if (MiljoUtils.isProdFss()) {
            getTokenForFss(target)
        } else {
            when (val tokenResponse = texasClient.getToken(IdentityProviders.AZURE_AD.value, target)) {
                is TokenResponse.Success -> tokenResponse.token
                is TokenResponse.Error ->
                    throw IllegalStateException("Failed to fetch token from Texas: $tokenResponse")
            }
        }
    }

    private fun getTokenForFss(target: String): String = runBlocking { azureadService.getSystemToken(target) }
}

private enum class IdentityProviders(val value: String) {
    AZURE_AD("azuread"),
    M2M("maskinporten"),
    TOKENX("tokenx"),
}

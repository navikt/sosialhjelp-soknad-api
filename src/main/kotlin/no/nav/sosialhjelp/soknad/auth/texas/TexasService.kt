package no.nav.sosialhjelp.soknad.auth.texas

import org.springframework.stereotype.Service

interface TexasService {
    fun getAzureAdToken(target: String): String
}

@Service
class TexasServiceImpl(
    private val texasClient: TexasClient,
) : TexasService {
    override fun getAzureAdToken(target: String): String {
        return when (val tokenResponse = texasClient.getToken(IdentityProviders.AZURE_AD.value, target)) {
            is TokenResponse.Success -> tokenResponse.token
            is TokenResponse.Error ->
                throw IllegalStateException("Failed to fetch token from Texas: $tokenResponse")
        }
    }
}

private enum class IdentityProviders(val value: String) {
    AZURE_AD("azuread"),
    M2M("maskinporten"),
    TOKENX("tokenx"),
}

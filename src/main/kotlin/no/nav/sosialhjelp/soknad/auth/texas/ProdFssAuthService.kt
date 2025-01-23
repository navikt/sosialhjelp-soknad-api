package no.nav.sosialhjelp.soknad.auth.texas

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient

class ProdFssAuthService(
    private val azureadService: AzureadService,
    private val maskinportenClient: MaskinportenClient,
) : TexasService {
    override fun getToken(
        idProvider: IdentityProvider,
        target: String,
    ): String {
        return runBlocking {
            when (idProvider) {
                IdentityProvider.AZURE_AD -> azureadService.getSystemToken(target)
                IdentityProvider.M2M -> maskinportenClient.getToken()
                else -> throw IllegalStateException("IdentityProvider not supported: $idProvider")
            }
        }
    }

    override fun exchangeToken(
        idProvider: IdentityProvider,
        target: String,
        userToken: String,
    ): String {
        TODO("Not yet implemented")
    }
}

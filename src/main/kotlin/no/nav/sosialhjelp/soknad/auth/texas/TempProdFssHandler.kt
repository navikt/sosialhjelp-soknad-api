package no.nav.sosialhjelp.soknad.auth.texas

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import org.springframework.stereotype.Component

@Component
class TempProdFssHandler(
    private val azureadService: AzureadService,
    private val maskinportenClient: MaskinportenClient,
) {
    fun handleProdFss(
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
}

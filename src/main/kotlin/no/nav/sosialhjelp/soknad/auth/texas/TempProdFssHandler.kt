package no.nav.sosialhjelp.soknad.auth.texas

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import org.springframework.stereotype.Component

@Component
class TempProdFssHandler(
    private val azureadService: AzureadService,
) {
    fun handleProdFss(
        idProvider: IdentityProvider,
        target: String,
    ): String {
        return runBlocking {
            when (idProvider) {
                IdentityProvider.AZURE_AD -> azureadService.getSystemToken(target)
                else -> throw IllegalStateException("IdentityProvider not supported: $idProvider")
            }
        }
    }
}

package no.nav.sosialhjelp.soknad.auth.texas

import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.auth.azure.AzureadService
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.auth.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getTokenOrNull as userTokenOrNull
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

class ProdFssAuthService(
    private val azureadService: AzureadService,
    private val maskinportenClient: MaskinportenClient,
    private val tokendingsService: TokendingsService,
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
    ): String {
        return runBlocking {
            userTokenOrNull()?.let { userToken ->
                when (idProvider) {
                    IdentityProvider.TOKENX -> tokendingsService.exchangeToken(personId(), userToken, target)
                    else -> throw IllegalStateException("IdentityProvider not supported: $idProvider")
                }
            } ?: throw IllegalStateException("User token not found")
        }
    }
}

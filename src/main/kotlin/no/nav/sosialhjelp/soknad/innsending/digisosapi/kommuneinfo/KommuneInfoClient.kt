package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createFiksHttpClient
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KommuneInfoClient(
    @param:Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @param:Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @param:Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    fun getAll(): List<KommuneInfo> {
        logger.info("Henter KommuneInfo fra FIKS")

        return kommuneInfoWebClient.get()
            .uri(PATH_ALLE_KOMMUNEINFO)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + m2mToken)
            .header(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
            .header(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
            .retrieve()
            .bodyToMono<List<KommuneInfo>>()
            .block()
            ?: emptyList()
    }

    private val kommuneInfoWebClient: WebClient =
        webClientBuilder.configureWebClientBuilder(createFiksHttpClient())
            .baseUrl(digisosApiEndpoint)
            .build()

    private val m2mToken get() = texasService.getToken(IdentityProvider.M2M, "ks:fiks")

    companion object {
        private val logger by logger()
        const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"
    }
}

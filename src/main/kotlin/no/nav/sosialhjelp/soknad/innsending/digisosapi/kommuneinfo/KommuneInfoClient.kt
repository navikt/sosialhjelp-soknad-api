package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

interface KommuneInfoClient {
    fun getAll(): List<KommuneInfo>
}

class KommuneInfoClientImpl(
    private val webClient: WebClient,
    private val maskinportenClient: MaskinportenClient,
    private val integrasjonsidFiks: String,
    private val integrasjonpassordFiks: String
) : KommuneInfoClient {

    override fun getAll(): List<KommuneInfo> {
        return webClient.get()
            .uri(PATH_ALLE_KOMMUNEINFO)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .header(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
            .header(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
            .retrieve()
            .bodyToMono(typeRef<List<KommuneInfo>>())
            .block()
            ?: emptyList()
    }

    private inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

    companion object {
        const val PATH_ALLE_KOMMUNEINFO = "/digisos/api/v1/nav/kommuner"
    }
}

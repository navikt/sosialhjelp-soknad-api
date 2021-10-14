package no.nav.sosialhjelp.soknad.client.maskinporten

import org.springframework.web.reactive.function.client.WebClient

interface MaskinportenClient {
    fun getTokenString(): String
}

class MaskinportenClientImpl(
    private val webClient: WebClient
) : MaskinportenClient {

    override fun getTokenString(): String {
        // todo: implement
        return "emptyString"
    }
}

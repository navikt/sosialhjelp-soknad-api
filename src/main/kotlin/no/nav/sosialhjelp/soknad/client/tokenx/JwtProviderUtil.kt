package no.nav.sosialhjelp.soknad.client.tokenx

import org.springframework.web.reactive.function.client.WebClient

object JwtProviderUtil {

    fun downloadWellKnown(url: String): WellKnown =
        WebClient.create()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(WellKnown::class.java)
            .block()
            ?: throw RuntimeException("Feiler under henting av well-known konfigurasjon fra $url")
}

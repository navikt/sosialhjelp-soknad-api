package no.nav.sosialhjelp.soknad.app.client.config

import org.slf4j.MDC
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

fun unproxiedWebClientBuilder(
    webClientBuilder: WebClient.Builder,
    httpClient: HttpClient = HttpClient.create(),
): WebClient.Builder =
    webClientBuilder
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .filter(mdcExchangeFilter)

val mdcExchangeFilter =
    ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
        // Kopierer MDC-context inn til reactor threads
        val map: Map<String, String>? = MDC.getCopyOfContextMap()
        next.exchange(request)
            .doOnNext {
                if (map != null) {
                    MDC.setContextMap(map)
                }
            }
    }

package no.nav.sosialhjelp.soknad.app.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import org.slf4j.MDC
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

fun unproxiedHttpClient(): HttpClient = HttpClient
    .newConnection()
    .resolver(DefaultAddressResolverGroup.INSTANCE)

fun unproxiedWebClientBuilder(webClientBuilder: WebClient.Builder): WebClient.Builder = webClientBuilder
    .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
    .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
    .filter(mdcExchangeFilter)

fun proxiedWebClientBuilder(webClientBuilder: WebClient.Builder, proxiedHttpClient: HttpClient): WebClient.Builder = webClientBuilder
    .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
    .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
    .filter(mdcExchangeFilter)

/**
 * MDC = Mapped Diagnostic Context. Kopierer fra tråden som gjorde requesten til reactor-threads som håndterer respons.
 */
val mdcExchangeFilter = ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
    val copyFromFirstThread = MDC.getCopyOfContextMap()
    next.exchange(request).doOnNext { copyFromFirstThread?.let { MDC.setContextMap(it) } }
}

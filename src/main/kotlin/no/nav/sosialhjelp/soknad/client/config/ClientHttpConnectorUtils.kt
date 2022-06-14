package no.nav.sosialhjelp.soknad.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

fun unproxiedHttpClient(): HttpClient = HttpClient
    .newConnection()
    .resolver(DefaultAddressResolverGroup.INSTANCE)

fun unproxiedWebClientBuilder(webClientBuilder: WebClient.Builder): WebClient.Builder {
    return webClientBuilder
        .clientConnector(ReactorClientHttpConnector(unproxiedHttpClient()))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
}

fun proxiedWebClientBuilder(webClientBuilder: WebClient.Builder, proxiedHttpClient: HttpClient): WebClient.Builder {
    return webClientBuilder
        .clientConnector(ReactorClientHttpConnector(proxiedHttpClient))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
}

package no.nav.sosialhjelp.soknad.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider
import java.net.URL

fun getProxiedReactorClientHttpConnector(proxyUrl: String): ReactorClientHttpConnector =
    ReactorClientHttpConnector(proxiedHttpClient(proxyUrl))

fun proxiedHttpClient(proxyUrl: String): HttpClient {
    val uri = URL(proxyUrl)

    return HttpClient.create()
        .resolver(DefaultAddressResolverGroup.INSTANCE)
        .proxy { proxy ->
            proxy.type(ProxyProvider.Proxy.HTTP).host(uri.host).port(uri.port)
        }
}

fun getUnproxiedReactorClientHttpConnector(): ReactorClientHttpConnector =
    ReactorClientHttpConnector(unproxiedHttpClient())

fun unproxiedHttpClient(): HttpClient = HttpClient
    .newConnection()
    .resolver(DefaultAddressResolverGroup.INSTANCE)

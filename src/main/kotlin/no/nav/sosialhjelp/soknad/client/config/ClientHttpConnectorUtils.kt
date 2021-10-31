package no.nav.sosialhjelp.soknad.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider
import java.net.URL

fun getProxiedReactorClientHttpConnector(proxyUrl: String): ReactorClientHttpConnector {
    val uri = URL(proxyUrl)

    val httpClient: HttpClient = HttpClient.create()
        .resolver(DefaultAddressResolverGroup.INSTANCE)
        .proxy { proxy ->
            proxy.type(ProxyProvider.Proxy.HTTP).host(uri.host).port(uri.port)
        }

    return ReactorClientHttpConnector(httpClient)
}

fun getUnproxiedReactorClientHttpConnector(): ReactorClientHttpConnector {
    val httpClient: HttpClient = HttpClient
        .newConnection()
        .resolver(DefaultAddressResolverGroup.INSTANCE)
    return ReactorClientHttpConnector(httpClient)
}

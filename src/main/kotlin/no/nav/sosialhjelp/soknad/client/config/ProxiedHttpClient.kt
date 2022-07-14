package no.nav.sosialhjelp.soknad.client.config

import io.netty.resolver.DefaultAddressResolverGroup
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider
import java.net.URL

object ProxiedHttpClient {

    fun proxiedHttpClient(proxyUrl: String?): HttpClient {
        val httpClient = HttpClient
            .create()
            .resolver(DefaultAddressResolverGroup.INSTANCE)

        if (proxyUrl.isNullOrBlank()) {
            return httpClient
        }
        return httpClient
            .proxy { proxy ->
                val uri = URL(proxyUrl)
                proxy.type(ProxyProvider.Proxy.HTTP).host(uri.host).port(uri.port)
            }
    }
}

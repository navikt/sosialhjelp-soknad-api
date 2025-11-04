package no.nav.sosialhjelp.soknad.app.client.config

import io.netty.channel.ChannelOption
import org.slf4j.MDC
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

// felles config for WebClients
fun configureWebClientBuilder(
    webClientBuilder: WebClient.Builder,
    httpClient: HttpClient,
): WebClient.Builder =
    webClientBuilder
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .filter(MdcExchangeFilter)

// konfigurarer HttpClient for bruk mot tjenester i fss-miljøet
fun createNavFssServiceHttpClient(): HttpClient =
    HttpClient.create(fssServiceConnectionProvider).option(ChannelOption.SO_KEEPALIVE, true)

fun createDefaultHttpClient(): HttpClient = HttpClient.create(defaultConnectionProvider)

fun createFiksHttpClient(): HttpClient = HttpClient.create(fiksServiceConnectionProvider)

private val defaultConnectionProvider: ConnectionProvider =
    ConnectionProvider.builder("default-connection-pool")
        .run {
            maxConnections(300)
            maxIdleTime(Duration.ofMinutes(30))
            evictInBackground(Duration.ofMinutes(5))
            metrics(true)
            build()
        }

// egen Connection pool for fss-tjenester da firewall sletter idle connections etter 60 minutter
// har potensielt skapt Connection timed out
private val fssServiceConnectionProvider: ConnectionProvider =
    ConnectionProvider.builder("fss-service-connection-pool")
        .run {
            maxConnections(500)
            maxIdleTime(Duration.ofMinutes(30))
            evictInBackground(Duration.ofMinutes(5))
            pendingAcquireTimeout(Duration.ofSeconds(10))
            metrics(true)
            build()
        }

// egen connection pool mot fiks-tjenester, da vi også opplever Connection reset by peer/Closed BEFORE response mot de
val fiksServiceConnectionProvider: ConnectionProvider =
    ConnectionProvider.builder("fiks-service-connection-pool")
        .run {
            maxConnections(300)
            maxIdleTime(Duration.ofMinutes(30))
            evictInBackground(Duration.ofMinutes(5))
            metrics(true)
            build()
        }

// Kopierer MDC-context inn til reactor threads
object MdcExchangeFilter : ExchangeFilterFunction {
    override fun filter(
        request: ClientRequest,
        next: ExchangeFunction,
    ): Mono<ClientResponse?> =
        next.exchange(request).doOnNext { setContextMap() }

    private fun setContextMap() = MDC.getCopyOfContextMap()?.also { MDC.setContextMap(it) }
}

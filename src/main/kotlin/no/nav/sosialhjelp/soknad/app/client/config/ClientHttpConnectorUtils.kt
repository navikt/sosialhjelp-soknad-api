package no.nav.sosialhjelp.soknad.app.client.config

import io.netty.channel.ChannelOption
import org.slf4j.MDC
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

// konfigurarer HttpClient for bruk mot tjenester i fss-miljøet
fun createNavFssServiceHttpClient(): HttpClient =
    HttpClient.create(fssServiceConnectionProvider)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofSeconds(10))

fun createDefaultHttpClient(): HttpClient =
    HttpClient.create(defaultConnectionProvider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
        .responseTimeout(Duration.ofSeconds(10))

fun configureWebClientBuilder(
    webClientBuilder: WebClient.Builder,
    httpClient: HttpClient,
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

private val defaultConnectionProvider: ConnectionProvider =
    ConnectionProvider.builder("fss-service-connection-pool")
        .run {
            maxIdleTime(Duration.ofMinutes(120))
            maxLifeTime(Duration.ofMinutes(240))
            evictInBackground(Duration.ofMinutes(10))
            pendingAcquireTimeout(Duration.ofSeconds(20))
            build()
        }

// egen Connection pool for fss-tjenester da firewall sletter idle connections etter 60 minutter
// har potensielt skapt Connection timed out
private val fssServiceConnectionProvider: ConnectionProvider =
    ConnectionProvider.builder("fss-service-connection-pool")
        .run {
            maxIdleTime(Duration.ofMinutes(55))
            maxLifeTime(Duration.ofMinutes(59))
            evictInBackground(Duration.ofMinutes(5))
            pendingAcquireTimeout(Duration.ofSeconds(10))
            build()
        }

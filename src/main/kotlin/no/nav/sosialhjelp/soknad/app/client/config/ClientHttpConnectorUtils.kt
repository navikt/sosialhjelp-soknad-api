package no.nav.sosialhjelp.soknad.app.client.config

import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.MDC
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

fun createNavServiceHttpClient(): HttpClient =
    HttpClient.create(navServiceConnectionProvider)
//        .option(ChannelOption.SO_KEEPALIVE, true)
//        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
//        .option(EpollChannelOption.TCP_KEEPIDLE, 3000)
//        .option(EpollChannelOption.TCP_KEEPINTVL, 60)
//        .option(EpollChannelOption.TCP_KEEPCNT, 8)
//        .responseTimeout(Duration.ofSeconds(30))
        .doOnConnected { conn ->
            conn.addHandlerFirst(IdleStateHandler(3000, 3000, 3000))
        }

fun createExternalServiceHttpClient(): HttpClient =
    HttpClient.create(externalServiceConnectionProvider)
        .doOnConnected { conn ->
            conn.addHandlerFirst(IdleStateHandler(3000, 3000, 3000))
        }

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

private val navServiceConnectionProvider =
    createConnectionProvider(
        name = "nav-service-connection-provider",
    )

private val externalServiceConnectionProvider =
    createConnectionProvider(
        name = "external-service-connection-provider",
        maxIdleTimeMinutes = Duration.ofMinutes(60),
        maxLifeTimeMinutes = Duration.ofMinutes(60),
    )

private fun createConnectionProvider(
    name: String,
    maxIdleTimeMinutes: Duration = Duration.ofMinutes(50),
    maxLifeTimeMinutes: Duration = Duration.ofMinutes(55),
    evictInBackgroundMinutes: Duration = Duration.ofMinutes(5),
    maxConnections: Int = 300,
    pendingAcquireMaxCount: Int = 600,
): ConnectionProvider {
    return ConnectionProvider.builder(name)
        .run {
            maxIdleTime(maxIdleTimeMinutes)
            maxLifeTime(maxLifeTimeMinutes)
            evictInBackground(evictInBackgroundMinutes)
            maxConnections(maxConnections)
            pendingAcquireMaxCount(pendingAcquireMaxCount)
            build()
        }
}

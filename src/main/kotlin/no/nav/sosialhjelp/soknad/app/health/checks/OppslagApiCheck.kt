package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppslagApiCheck(
    @Value("\${oppslag_api_baseurl}") private val oppslagApiUrl: String,
    webClientBuilder: WebClient.Builder,
) : DependencyCheck {
    private val pingurl = "${oppslagApiUrl}ping"

    override val type = DependencyType.REST
    override val name = "sosialhjelp-oppslag-api (proxy for soaptjenestene person_v3 og utbetalingWS)"
    override val address = pingurl
    override val importance = Importance.WARNING

    private val oppslagApiWebClient: WebClient = unproxiedWebClientBuilder(webClientBuilder).build()

    override fun doCheck() {
        oppslagApiWebClient.get()
            .uri(pingurl)
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap {
                throw RuntimeException("Ping mot sosialhjelp-oppslag-api feiler: ${it.message}", it)
            }
            .block()
    }
}

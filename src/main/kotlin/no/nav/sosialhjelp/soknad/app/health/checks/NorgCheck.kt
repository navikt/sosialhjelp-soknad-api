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
class NorgCheck(
    @Value("\${norg_url}") private val norgUrl: String,
    webClientBuilder: WebClient.Builder,
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "Norg"
    override val address = norgUrl
    override val importance = Importance.WARNING

    private val norgWebClient: WebClient = unproxiedWebClientBuilder(webClientBuilder).build()

    override fun doCheck() {
        norgWebClient.get()
            .uri("$norgUrl/kodeverk/EnhetstyperNorg")
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap {
                throw RuntimeException("Ping mot Norg feiler: ${it.message}", it)
            }
            .block()
    }
}

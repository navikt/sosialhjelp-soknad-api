package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CALL_ID
import no.nav.sosialhjelp.soknad.app.Constants.HEADER_CONSUMER_ID
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations.generateCallId
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getConsumerId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KodeverkCheck(
    @Value("\${kodeverk_url}") private val kodeverkUrl: String,
    webClientBuilder: WebClient.Builder,
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "Kodeverk"
    override val address = kodeverkUrl
    override val importance = Importance.WARNING

    private val kodeverkWebClient: WebClient = unproxiedWebClientBuilder(webClientBuilder).build()

    override fun doCheck() {
        kodeverkWebClient.get()
            .uri(kodeverkUrl)
            .header(HEADER_CALL_ID, generateCallId())
            .header(HEADER_CONSUMER_ID, getConsumerId())
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap {
                throw RuntimeException("Ping mot Kodeverk feiler: ${it.message}", it)
            }
            .block()
    }
}

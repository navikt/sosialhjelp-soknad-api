package no.nav.sosialhjelp.soknad.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.MDC_CALL_ID
import no.nav.sosialhjelp.soknad.common.mdc.MdcOperations.getFromMDC
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils.getConsumerId
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
            .header(Constants.HEADER_CALL_ID, getFromMDC(MDC_CALL_ID))
            .header(Constants.HEADER_CONSUMER_ID, getConsumerId())
            .retrieve()
            .bodyToMono<String>()
            .onErrorMap {
                throw RuntimeException("Ping mot Kodeverk feiler: ${it.message}", it)
            }
            .block()
    }
}

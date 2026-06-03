package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class KontonummerService(
    private val kontonummerClient: KontonummerClient,
) {
    /**
     * Henter norsk kontonummer fra kontoregister.
     *
     * @return Kontonummer 11 siffer dersom norsk, null dersom utenlandsk eller ikke funnet.
     */
    @WithSpan("fetch kontonummer from kontoregister")
    suspend fun getKontonummer(): String? {
        log.info("Henter kontonummmer fra kontoregister")

        return when (val kontoResponse = kontonummerClient.getKontonummer()) {
            is KontoResponse.Success -> {
                val konto = kontoResponse.kontoDto
                if (konto.utenlandskKontoInfo != null) {
                    // Dersom utenlandskKontoInfo ikke er null, vil "kontonummer" være et utenlandsk kontonummer.
                    log.info("Kontonummer fra kontoregister er utenlandskonto og kontonummer settes ikke")
                    null
                } else {
                    konto.kontonummer
                }
            }

            is KontoResponse.Error -> {
                when (val throwable = kontoResponse.throwable) {
                    is WebClientResponseException.NotFound -> log.info("Fant ingen konto i kontoregister - ${throwable.message}")
                    else -> {
                        log.error("Kontoregister konto - Noe uventet feilet", throwable)
                        Span.current().recordException(throwable)
                        Span.current().setStatus(StatusCode.ERROR)
                    }
                }
                null
            }

            KontoResponse.Null -> null
        }
    }

    companion object {
        private val log by logger()
    }
}

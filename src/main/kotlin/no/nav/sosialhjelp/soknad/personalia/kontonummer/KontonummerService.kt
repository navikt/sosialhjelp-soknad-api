package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
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

        return when (val response = kontonummerClient.getKontonummer()) {
            is KontoResponse.Success -> handleSuccess(response.kontoDto)
            is KontoResponse.Error -> {
                handleError(response)
                null
            }
        }
    }

    private fun handleSuccess(dto: KontoDto): String? =
        if (dto.utenlandskKontoInfo == null) {
            dto.kontonummer
        } else {
            // Dersom utenlandskKontoInfo ikke er null, vil "kontonummer" være et utenlandsk kontonummer.
            log.info("Kontonummer fra kontoregister er utenlandskonto og kontonummer settes ikke")
            null
        }

    private fun handleError(response: KontoResponse.Error) {
        when {
            response.throwable is WebClientResponseException.NotFound -> {
                log.info("Fant ingen konto i kontoregister")
                Span.current().addEvent(
                    "Fant ingen konto i kontoregister",
                    Attributes.of(AttributeKey<Int>.stringKey("StatusCode"), "404 Not Found"),
                )
            }
            else -> {
                log.error("Kontoregister konto - Noe uventet feilet", response.throwable)
                Span.current().recordException(response.throwable)
                Span.current().setStatus(StatusCode.ERROR)
            }
        }
    }

    companion object {
        private val log by logger()
    }
}

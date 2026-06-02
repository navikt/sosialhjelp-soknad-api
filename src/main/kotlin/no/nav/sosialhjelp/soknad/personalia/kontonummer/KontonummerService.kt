package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component

@Component
class KontonummerService(
    private val kontonummerClient: KontonummerClient,
) {
    /**
     * Henter norsk kontonummer fra kontoregister.
     *
     * @return Kontonummer 11 siffer dersom norsk, null dersom utenlandsk eller ikke funnet.
     */
    @WithSpan("Fetching kontonummer from Kontoregister")
    suspend fun getKontonummer(): String? {
        log.info("Henter kontonummmer fra kontoregister")

        val konto =
            runCatching { kontonummerClient.getKontonummer() }
                .onFailure {
                    Span.current().recordException(it)
                    Span.current().setStatus(StatusCode.ERROR)
                    throw it
                }
                .getOrNull()

        return when {
            konto == null -> null

            konto.utenlandskKontoInfo != null -> {
                // Dersom utenlandskKontoInfo ikke er null, vil "kontonummer" være et utenlandsk kontonummer.
                log.info("Kontonummer fra kontoregister er utenlandskonto og kontonummer settes ikke")
                null
            }

            else -> konto.kontonummer
        }
    }

    companion object {
        private val log by logger()
    }
}

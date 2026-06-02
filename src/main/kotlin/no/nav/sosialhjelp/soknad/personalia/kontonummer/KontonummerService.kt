package no.nav.sosialhjelp.soknad.personalia.kontonummer

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
    suspend fun getKontonummer(): String? {
        log.info("Henter kontonummmer fra kontoregister")

        val konto = kontonummerClient.getKontonummer()

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

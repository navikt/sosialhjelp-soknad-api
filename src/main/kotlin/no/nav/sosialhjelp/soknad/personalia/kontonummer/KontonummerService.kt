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
     * @param userToken Personidentifikator (fødselsnummer eller d-nummer)
     * @return Kontonummer 11 siffer dersom norsk, null dersom utenlandsk eller ikke funnet.
     */
    fun getKontonummer(userToken: String): String? {
        log.info("Henter kontonummmer fra kontoregister")

        val konto = kontonummerClient.getKontonummer(userToken)

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

package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component

@Component
class KontonummerService(
    private val kontonummerClient: KontonummerClient
) {

    fun getKontonummer(ident: String): String? {
        log.info("Henter kontonummmer fra kontoregister")
        val konto = kontonummerClient.getKontonummer(ident)
        return if (konto?.utenlandskKontoInfo != null) {
            log.info("Kontonummer fra kontoregister er utenlandskonto og kontonummer settes ikke")
            null
        } else {
            konto?.kontonummer
        }
    }

    companion object {
        private val log by logger()
    }
}

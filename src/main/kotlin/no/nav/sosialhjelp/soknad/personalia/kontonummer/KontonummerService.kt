package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.finn.unleash.Unleash
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class KontonummerService(
    private val kontonummerClient: KontonummerClient,
    private val unleash: Unleash
) {

    open fun getKontonummer(ident: String): String? {
        if (unleash.isEnabled(BRUK_KONTOREGISTER_ENABLED, true)) {
            log.info("Feature toggle for bruk av kontoregister er enablet og kontonummer hentes fra kontoregister")
            return kontonummerClient.getKontonummer(ident)?.kontonummer
        } else {
            log.info("Feature toggle for bruk av kontoregister er disablet og kontonummer hentes fra PersonV3 tjeneste via oppslag-api")
            return kontonummerClient.getKontonummerLegacy(ident)?.kontonummer
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(KontonummerService::class.java)
        const val BRUK_KONTOREGISTER_ENABLED = "sosialhjelp.soknad.bruk_sokos_kontoregister"
    }
}

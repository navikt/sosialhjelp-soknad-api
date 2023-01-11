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
        val kontonummer: String?
        if (unleash.isEnabled(BRUK_KONTOREGISTER_ENABLED, true)) {
            log.info("Feature toggle for bruk av kontoregister er enablet og kontonummer hentes fra kontoregister")
            kontonummer = kontonummerClient.getKontonummer(ident)?.kontonummer
        } else {
            log.info("Feature toggle for bruk av kontoregister er disablet og kontonummer hentes fra PersonV3 tjeneste via oppslag-api")
            kontonummer = kontonummerClient.getKontonummerLegacy(ident)?.kontonummer
            sammenlignmedSkyggeproduksjon(kontonummer, ident)
        }

        return kontonummer
    }

    private fun sammenlignmedSkyggeproduksjon(kontonummer: String?, ident: String) {
        val kontonummerSkygge = kontonummerClient.getKontonummer(ident)?.kontonummer

        if (kontonummerSkygge == kontonummer) {
            log.info("Kontoregister skyggeproduksjon: Kontonummer fra PersonV3 tjeneste og kontonummer fra kontoregister er like")
        } else {
            log.info(
                "Kontoregister skyggeproduksjon: Kontonummer fra PersonV3 tjeneste og kontonummer fra kontoregister er forskjellige." +
                    "Kontonummer fra PersonV3: $kontonummer og kontonummer fra Kontoregister: $kontonummerSkygge"
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(KontonummerService::class.java)
        const val BRUK_KONTOREGISTER_ENABLED = "sosialhjelp.soknad.bruk_sokos_kontoregister"
    }
}

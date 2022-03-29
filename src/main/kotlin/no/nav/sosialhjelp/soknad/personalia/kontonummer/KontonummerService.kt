package no.nav.sosialhjelp.soknad.personalia.kontonummer

import org.springframework.stereotype.Component

@Component
open class KontonummerService(
    private val kontonummerClient: KontonummerClient
) {

    open fun getKontonummer(ident: String): String? {
        return kontonummerClient.getKontonummer(ident)?.kontonummer
    }
}

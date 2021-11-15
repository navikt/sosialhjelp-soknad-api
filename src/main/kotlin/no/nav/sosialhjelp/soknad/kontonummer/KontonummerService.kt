package no.nav.sosialhjelp.soknad.kontonummer

open class KontonummerService(
    private val kontonummerClient: KontonummerClient
) {

    open fun getKontonummer(ident: String): String? {
        return kontonummerClient.getKontonummer(ident)?.kontonummer
    }
}

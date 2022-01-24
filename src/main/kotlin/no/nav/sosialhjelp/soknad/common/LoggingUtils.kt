package no.nav.sosialhjelp.soknad.common

object LoggingUtils {
    fun maskerFnr(tekst: String?): String? {
        return tekst?.replace("\\b[0-9]{11}\\b".toRegex(), "[FNR]")
    }
}

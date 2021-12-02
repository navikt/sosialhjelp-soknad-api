package no.nav.sosialhjelp.soknad.api

object LenkeUtils {
    fun lenkeTilPabegyntSoknad(behandlingsId: String): String {
        return lagContextLenke() + "skjema/" + behandlingsId + "/0"
    }

    fun lagEttersendelseLenke(behandlingsId: String): String? {
        return lagContextLenke() + "skjema/" + behandlingsId + "/ettersendelse"
    }

    fun lagFortsettSoknadLenke(behandlingsId: String): String? {
        return lagContextLenke() + "skjema/" + behandlingsId + "/0"
    }

    private fun lagContextLenke(): String {
        val miljo = System.getProperty("environment.name", "")
        val postfix = if (miljo.contains("q")) "-$miljo.dev" else ""
        return "https://www$postfix.nav.no/sosialhjelp/soknad/"
    }
}

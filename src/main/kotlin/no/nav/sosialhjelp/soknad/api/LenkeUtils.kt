package no.nav.sosialhjelp.soknad.api

object LenkeUtils {
    fun lenkeTilPabegyntSoknad(behandlingsId: String, environmentName: String): String {
        return lagContextLenke(environmentName) + "skjema/" + behandlingsId + "/0"
    }

    fun lagEttersendelseLenke(behandlingsId: String, environmentName: String): String {
        return lagContextLenke(environmentName) + "skjema/" + behandlingsId + "/ettersendelse"
    }

    private fun lagContextLenke(environmentName: String): String {
        val postfix = if (environmentName.contains("q")) "-$environmentName.dev" else ""
        return "https://www$postfix.nav.no/sosialhjelp/soknad/"
    }
}

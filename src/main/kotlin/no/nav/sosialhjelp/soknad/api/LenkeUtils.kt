package no.nav.sosialhjelp.soknad.api

object LenkeUtils {
    fun lenkeTilPabegyntSoknad(behandlingsId: String, env: String): String {
        return lagContextLenke(env) + "skjema/" + behandlingsId + "/0"
    }

    fun lagEttersendelseLenke(behandlingsId: String, env: String): String {
        return lagContextLenke(env) + "skjema/" + behandlingsId + "/ettersendelse"
    }

    private fun lagContextLenke(env: String): String {
        val postfix = if (env.contains("q")) "-$env.dev" else ""
        return "https://www$postfix.nav.no/sosialhjelp/soknad/"
    }
}

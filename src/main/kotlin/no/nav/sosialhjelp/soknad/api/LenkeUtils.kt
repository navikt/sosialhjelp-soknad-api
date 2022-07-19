package no.nav.sosialhjelp.soknad.api

import no.nav.sosialhjelp.soknad.app.MiljoUtils

object LenkeUtils {
    fun lenkeTilPabegyntSoknad(behandlingsId: String): String {
        return "$lagContextLenke/skjema/$behandlingsId/0"
    }

    fun lagEttersendelseLenke(behandlingsId: String): String {
        return "$lagContextLenke/skjema/$behandlingsId/ettersendelse"
    }

    private val lagContextLenke: String
        get() {
            val environmentName = MiljoUtils.environmentName
            val postfix = if (environmentName.contains("q")) "-$environmentName.dev" else ""
            return "https://www$postfix.nav.no/sosialhjelp/soknad"
        }
}

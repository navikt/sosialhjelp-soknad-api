package no.nav.sosialhjelp.soknad.api

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import java.util.UUID

object LenkeUtils {
    fun lenkeTilPabegyntSoknad(soknadId: UUID): String {
        return "$lagContextLenke/skjema/$soknadId/1"
    }

    private val lagContextLenke: String
        get() {
            val environmentName = MiljoUtils.environmentName
            val postfix = if (environmentName.contains("q")) "-$environmentName.dev" else ""
            return "https://www$postfix.nav.no/sosialhjelp/soknad"
        }
}

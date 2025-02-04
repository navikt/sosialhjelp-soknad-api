package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.app.MiljoUtils

// todo enhetlig løsning eller fjern helt
object SenderUtils {
    fun createPrefixedBehandlingsId(behandlingsId: String?): String {
        return "${MiljoUtils.environmentName}-$behandlingsId"
    }

    const val SKJEMANUMMER = "NAV 35-18.01"
}

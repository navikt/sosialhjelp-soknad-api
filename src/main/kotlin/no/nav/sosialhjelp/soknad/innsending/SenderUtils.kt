package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.common.MiljoUtils

object SenderUtils {
    fun createPrefixedBehandlingsId(behandlingsId: String?): String {
        return "${MiljoUtils.environmentName}-$behandlingsId"
    }
}

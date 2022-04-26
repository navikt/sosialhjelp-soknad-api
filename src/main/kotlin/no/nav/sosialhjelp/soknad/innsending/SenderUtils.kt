package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.common.MiljoUtils

object SenderUtils {
    fun createPrefixedBehandlingsId(behandlingsId: String?): String {
        return "${MiljoUtils.environmentName}-$behandlingsId"
    }

    const val INNSENDING_DIGISOSAPI_ENABLED = "sosialhjelp.soknad.innsending-digisosapi-enabled"
}

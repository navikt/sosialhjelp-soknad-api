package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils

object SenderUtils {
    fun createPrefixedBehandlingsIdInNonProd(behandlingsId: String?): String? {
        return if (ServiceUtils.isNonProduction()) {
            System.getProperty("environment.name") + "-" + behandlingsId
        } else behandlingsId
    }
}

package no.nav.sosialhjelp.soknad.innsending

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.mdc.MdcOperations
import java.util.Locale

object SenderUtils {

    fun lagBehandlingsId(databasenokkel: Long): String {
        val applikasjonsprefix = "11"
        val base = (applikasjonsprefix + "0000000").toLong(36)
        val behandlingsId = (base + databasenokkel).toString(36).uppercase(Locale.getDefault())
            .replace("O", "o").replace("I", "i")
        if (!behandlingsId.startsWith(applikasjonsprefix)) {
            throw SosialhjelpSoknadApiException("Tildelt sekvensrom for behandlingsId er brukt opp. Kan ikke generer behandlingsId $behandlingsId")
        }
        MdcOperations.putToMDC(MdcOperations.MDC_BEHANDLINGS_ID, behandlingsId)
        return behandlingsId
    }

    fun createPrefixedBehandlingsId(behandlingsId: String?): String {
        return "${MiljoUtils.environmentName}-$behandlingsId"
    }

    const val SKJEMANUMMER = "NAV 35-18.01"

    const val INNSENDING_DIGISOSAPI_ENABLED = "sosialhjelp.soknad.innsending-digisosapi-enabled"
}

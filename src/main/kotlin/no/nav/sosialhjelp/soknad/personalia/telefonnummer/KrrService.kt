package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.springframework.stereotype.Component

@Component
class KrrService(
    private val krrClient: KrrClient,
) {
    suspend fun getMobilnummer(): String? {
        return doGet()
            ?.also { it.mobiltelefonnummer ?: logger.warn("KRR - mobiltelefonnummer er null") }
            ?.mobiltelefonnummer
    }

    private suspend fun doGet(): DigitalKontaktinformasjon? {
        val kontaktInfoResponse = krrClient.getDigitalKontaktinformasjon() ?: return null

        return kontaktInfoResponse.personer
            ?.let { infoForPersonMap -> infoForPersonMap[currentUserContext().userId] }
            .also { info -> if (info == null) kontaktInfoResponse.logError(currentUserContext().userId) }
    }

    private fun KontaktInfoResponse.logError(personId: String) {
        feil?.get(personId)
            ?.also { message ->
                when (message) {
                    IKKE_FUNNET -> logger.warn("Person finnes ikke i KRR")
                    // Selvom vi aldri bør komme hit hvis en person er skjermet, vil meldingen kunne inneholde skjermet status
                    else -> logger.error("Kunne ikke hente fra KRR")
                }
            }
    }

    companion object {
        private val logger by logger()
        const val IKKE_FUNNET = "person_ikke_funnet"
    }
}

data class DigitalKontaktinformasjon(
    val personident: String,
    val aktiv: Boolean,
    val kanVarsles: Boolean?,
    val reservert: Boolean?,
    val mobiltelefonnummer: String?,
)

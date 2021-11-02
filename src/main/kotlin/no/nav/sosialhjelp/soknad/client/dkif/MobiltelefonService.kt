package no.nav.sosialhjelp.soknad.client.dkif

import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Component
open class MobiltelefonService(
    private val dkifClient: DkifClient
) {

    fun hent(ident: String): String? {
        val digitalKontaktinfoBolk = dkifClient.hentDigitalKontaktinfo(ident)
        if (digitalKontaktinfoBolk == null) {
            log.warn("Dkif.api - response er null")
            return null
        }
        if (digitalKontaktinfoBolk.feil != null) {
            log.warn("Dkif.api - response inneholder feil - {}", digitalKontaktinfoBolk.feil[ident]!!.melding)
            return null
        }
        if (digitalKontaktinfoBolk.kontaktinfo == null || digitalKontaktinfoBolk.kontaktinfo.isEmpty() || !digitalKontaktinfoBolk.kontaktinfo.containsKey(ident) || digitalKontaktinfoBolk.kontaktinfo[ident]!!.mobiltelefonnummer == null) {
            log.warn("Dkif.api - kontaktinfo er null, eller mobiltelefonnummer er null")
            return null
        }
        return digitalKontaktinfoBolk.kontaktinfo[ident]!!.mobiltelefonnummer
    }

    companion object {
        private val log = getLogger(MobiltelefonService::class.java)
    }
}

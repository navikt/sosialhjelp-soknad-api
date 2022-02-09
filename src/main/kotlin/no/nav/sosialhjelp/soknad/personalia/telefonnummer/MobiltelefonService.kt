package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.finn.unleash.Unleash
import org.slf4j.LoggerFactory.getLogger

interface MobiltelefonService {
    fun hent(ident: String): String?
}

class MobiltelefonServiceImpl(
    private val dkifClient: DkifClient,
    private val krrClient: KrrClient,
    private val unleash: Unleash
) : MobiltelefonService {

    override fun hent(ident: String): String? {
        return if (unleash.isEnabled(brukKrrOverDkif, false)) {
            hentFraKrr(ident)
        } else {
            hentFraDkif(ident)
        }
    }

    private fun hentFraDkif(ident: String): String? {
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

    private fun hentFraKrr(ident: String): String? {
        val digitalKontaktinformasjon = krrClient.getDigitalKontaktinformasjon(ident)
        if (digitalKontaktinformasjon == null) {
            log.warn("Krr - response er null")
            return null
        }
        if (digitalKontaktinformasjon.mobiltelefonnummer == null) {
            log.warn("Krr - mobiltelefonnummer er null")
            return null
        }
        return digitalKontaktinformasjon.mobiltelefonnummer
    }

    companion object {
        private val log = getLogger(MobiltelefonServiceImpl::class.java)

        private const val brukKrrOverDkif = "sosialhjelp.soknad.bruk-krr-over-dkif"
    }
}

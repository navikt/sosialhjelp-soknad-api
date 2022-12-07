package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

interface MobiltelefonService {
    fun hent(ident: String): String?
}

@Component
class MobiltelefonServiceImpl(
    private val krrService: KrrService,
) : MobiltelefonService {

    override fun hent(ident: String): String? {
        val digitalKontaktinformasjon = krrService.getDigitalKontaktinformasjon(ident)
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
    }
}

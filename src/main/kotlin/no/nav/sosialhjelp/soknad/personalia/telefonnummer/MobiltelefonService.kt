package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.springframework.stereotype.Component

@Component
class MobiltelefonService(
    private val krrService: KrrService,
) {
    fun hent(ident: String): String? {
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
        private val log by logger()
    }
}

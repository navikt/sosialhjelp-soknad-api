package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SoknadValidator(
    private val adresseService: AdresseService,
) {
    fun validateAndReturnMottaker(soknadId: UUID): NavEnhet {
        return adresseService.findMottaker(soknadId)
            ?.also {
                logger.info(
                    "Skal sendes til kommune ${it.kommunenummer}) med " +
                        "enhetsnummer ${it.enhetsnummer} og navenhetsnavn ${it.enhetsnavn}",
                )
            }
            ?: throw IllegalStateException("Søknad mangler mottaker (NavEnhet)")
    }

    companion object {
        private val logger by logger()
    }
}

package no.nav.sosialhjelp.soknad.v2

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SoknadValidator(
    private val adresseService: AdresseService,
    private val mellomlagringService: MellomlagringService,
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

    fun validateAndReturnJsonString(
        soknadId: UUID,
        jsonVedleggSpec: JsonVedleggSpesifikasjon,
    ): String {
        // TODO Validate mot mellomlagring

        mellomlagringService.getAllVedlegg(soknadId).forEach {
            it.filId
        }

        return objectMapper.writeValueAsString(jsonVedleggSpec)
            .also { JsonSosialhjelpValidator.ensureValidVedlegg(it) }
    }

    private fun validateAllFilerExists() {
    }

    private fun deleteMellomlagredeWithoutLocalReference() {
    }

    companion object {
        private val logger by logger()
        private val objectMapper = JsonSosialhjelpObjectMapper.createObjectMapper()
    }
}

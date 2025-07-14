package no.nav.sosialhjelp.soknad.v2.bostotte

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.register.fetchers.HusbankenService
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BostotteUseCaseHandler(
    private val husbankenService: HusbankenService,
    private val bostotteService: BostotteService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) {
    fun getBostotteInfo(soknadId: UUID): BostotteInfo = bostotteService.getBostotteInfo(soknadId)

    fun updateBostotte(
        soknadId: UUID,
        hasBostotte: Boolean?,
        hasSamtykke: Boolean?,
    ) {
        if (hasBostotte != null && hasBostotte != existingHasBostotte(soknadId)) {
            hasBostotte.also { bostotteService.updateBostotte(soknadId, hasBostotte) }
        }
        if (hasBostotte == true || existingHasBostotte(soknadId) == true) hasSamtykke?.also { updateSamtykke(soknadId, it) }
    }

    private fun updateSamtykke(
        soknadId: UUID,
        hasSamtykke: Boolean,
    ) {
        if (doNotUpdateSamtykke(soknadId, hasSamtykke)) return

        bostotteService.updateSamtykke(soknadId, hasSamtykke)
        if (hasSamtykke) handleGetFromHusbanken(soknadId)
    }

    // hvis samtykke er uendret, og henting fra husbanken ikke har feilet
    private fun doNotUpdateSamtykke(
        soknadId: UUID,
        hasSamtykke: Boolean,
    ): Boolean =
        hasSamtykke == existingHasSamtykke(soknadId) && integrasjonStatusService.hasFetchHusbankenFailed(soknadId) == false

    private fun handleGetFromHusbanken(soknadId: UUID) {
        runCatching { husbankenService.getBostotte() }
            .onSuccess { (saker, inntekt) ->
                integrasjonStatusService.setStotteHusbankenStatus(soknadId, false)

                when {
                    saker.isEmpty() && inntekt == null -> logger.info("Fant ingen data hos Husbanken")
                    else -> {
                        logger.info("Hentet data fra Husbanken")
                        bostotteService.saveDataFromHusbanken(soknadId, saker, inntekt)
                    }
                }
            }
            .onFailure {
                logger.error("Henting fra Husbanken feilet", it)

                // gir bruker mulighet til å legge ved denne informasjonen selv
                bostotteService.addForventetDokumentasjon(soknadId)
                integrasjonStatusService.setStotteHusbankenStatus(soknadId, true)
            }
    }

    private fun existingHasBostotte(soknadId: UUID): Boolean? = getBostotteInfo(soknadId).bostotte?.verdi

    private fun existingHasSamtykke(soknadId: UUID): Boolean? = getBostotteInfo(soknadId).samtykke?.verdi

    companion object {
        private val logger by logger()
    }
}

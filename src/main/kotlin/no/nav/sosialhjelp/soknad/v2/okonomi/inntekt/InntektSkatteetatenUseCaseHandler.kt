package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.register.fetchers.InntektSkatteetatenFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InntektSkatteetatenUseCaseHandler(
    private val inntektSkatteetatenService: InntektSkatteetatenService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val inntektSkatteetatenFetcher: InntektSkatteetatenFetcher,
) {
    fun getInntektSkattInfo(soknadId: UUID): InntektSkattInfo {
        return InntektSkattInfo(
            inntekt = inntektSkatteetatenService.getInntektSkatt(soknadId),
            getFailed = integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknadId) ?: false,
            samtykke = inntektSkatteetatenService.getSamtykkeSkatt(soknadId),
        )
    }

    fun updateSamtykke(
        soknadId: UUID,
        hasSamtykke: Boolean,
    ) {
        if (doesNotNeedUpdate(soknadId, hasSamtykke)) return

        inntektSkatteetatenService.updateSamtykkeSkatt(soknadId, hasSamtykke)

        if (hasSamtykke) {
            runCatching { handleFetchFromSkatt(soknadId) }
                .onSuccess { integrasjonStatusService.setInntektSkatteetatenStatus(soknadId, false) }
                .onFailure { integrasjonStatusService.setInntektSkatteetatenStatus(soknadId, true) }
        } else {
            integrasjonStatusService.setInntektSkatteetatenStatus(soknadId, false)
            inntektSkatteetatenService.createJobbElement(soknadId)
        }
    }

    private fun doesNotNeedUpdate(
        soknadId: UUID,
        hasSamtykke: Boolean,
    ): Boolean {
        return hasSamtykke == getExistingSamtykke(soknadId)?.verdi &&
            integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknadId) == false
    }

    private fun handleFetchFromSkatt(soknadId: UUID) {
        runCatching { inntektSkatteetatenFetcher.fetchInntekt() }
            .onSuccess { utbetalinger -> inntektSkatteetatenService.saveUtbetalinger(soknadId, utbetalinger) }
            .onFailure { ex ->
                logger.error("Fetching fra Skatteetaten feilet", ex)
                inntektSkatteetatenService.createJobbElement(soknadId)
                throw ex
            }
    }

    private fun getExistingSamtykke(soknadId: UUID): Bekreftelse? {
        return inntektSkatteetatenService.getSamtykkeSkatt(soknadId = soknadId)
    }

    companion object {
        private val logger by logger()
    }
}

data class InntektSkattInfo(
    val inntekt: Inntekt?,
    val getFailed: Boolean,
    val samtykke: Bekreftelse?,
)

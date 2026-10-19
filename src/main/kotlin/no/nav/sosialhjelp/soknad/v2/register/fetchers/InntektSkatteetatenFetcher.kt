package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektSkatteetatenService
import no.nav.sosialhjelp.soknad.v2.register.AsynchronousFetcher
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling as OldUtbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling as V2Utbetaling

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class InntektSkatteetatenFetcher(
    private val skattbarInntektService: SkattbarInntektService,
    private val inntektSkatteetatenService: InntektSkatteetatenService,
    private val organisasjonService: OrganisasjonService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) : AsynchronousFetcher {
    private val logger by logger()

    override suspend fun fetchAndSave(soknadId: UUID) {
        runCatching {
            val utbetalinger = fetchInntekt()
            inntektSkatteetatenService.saveUtbetalinger(soknadId, utbetalinger)
            integrasjonStatusService.setInntektSkatteetatenStatus(soknadId, false)
        }
            .onFailure { ex ->
                logger.error("Fetching fra Skatteetaten feilet: ${ex.message}", ex)
                integrasjonStatusService.setInntektSkatteetatenStatus(soknadId, true)
                inntektSkatteetatenService.createJobbElement(soknadId)
            }
    }

    suspend fun fetchInntekt(): List<V2Utbetaling> {
        return skattbarInntektService
            .hentInntekt()
            .map { it.toUtbetalingDomain() }
    }

    private fun OldUtbetaling.toUtbetalingDomain() =
        V2Utbetaling(
            brutto = brutto,
            skattetrekk = skattetrekk,
            periodeFom = periodeFom,
            periodeTom = periodeTom,
            tittel = tittel,
            organisasjon =
                Organisasjon(
                    navn = organisasjonService.hentOrgNavn(orgnummer),
                    orgnummer = orgnummer,
                ),
        )
}

data class SkatteetatenException(
    override val message: String?,
    override val cause: Throwable? = null,
    val soknadId: UUID? = null,
) : SosialhjelpSoknadApiException(message, cause, soknadId.toString())

package no.nav.sosialhjelp.soknad.v2.register.fetchers

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling as OldUtbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling as V2Utbetaling

@Component
class InntektSkatteetatenFetcher(
    private val skattbarInntektService: SkattbarInntektService,
    private val organisasjonService: OrganisasjonService,
) {
    @WithSpan("Fetching inntekt from Skatteetaten")
    fun fetchInntekt(): List<V2Utbetaling> {
        return runCatching { skattbarInntektService.hentUtbetalinger(getUserIdFromToken()) }
            .getOrElse {
                Span.current().recordException(it)
                Span.current().setStatus(StatusCode.ERROR)

                throw it
            }
            ?.map { it.toUtbetalingDomain() }
            ?: throw SkatteetatenException("Fetch av inntekt fra Skatteetaten var null")
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

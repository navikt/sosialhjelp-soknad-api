package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling as V2Utbetaling

@Component
class InntektSkatteetatenFetcher(
    private val skattbarInntektService: SkattbarInntektService,
    private val organisasjonService: OrganisasjonService,
) {
    fun fetch(): List<no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling> {
        return skattbarInntektService
            .hentUtbetalinger(getUserIdFromToken())
            ?.map { it.toUtbetalingDomain() }
            ?: throw SkatteetatenException("Fetch av inntekt fra Skatteetaten var null")
    }

    private fun Utbetaling.toUtbetalingDomain() =
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

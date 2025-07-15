package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenResponse
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Bostotte
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Sak
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Utbetaling as UtbetalingHusbanken

@Component
class HusbankenService(
    private val husbankenClient: HusbankenClient,
) {
    fun getBostotte(): Pair<List<BostotteSak>, Inntekt?> {
        return doGetBostotte(LocalDate.now().minusDays(60), LocalDate.now())
            .toDomain()
            .let { Pair(saveToSaker(it), saveToInntekt(it)) }
    }

    private fun doGetBostotte(
        fra: LocalDate = LocalDate.now().minusDays(60),
        til: LocalDate = LocalDate.now(),
    ): BostotteDto =
        when (val response = husbankenClient.getBostotte(fra, til)) {
            is HusbankenResponse.Success -> response.bostotte
            is HusbankenResponse.Error -> throw HusbankenException(
                melding = resolveErrorMessage(response.e),
                cause = response.e,
            )
            is HusbankenResponse.Null -> error("Response from Husbanken was null")
        }

    private fun saveToSaker(bostotte: Bostotte): List<BostotteSak> =
        bostotte.saker
            .filter { it.dato.isAfter(LocalDate.now().minusDays(daysToSubtract(bostotte))) }
            .map { it.toBostotteSak() }

    private fun saveToInntekt(bostotte: Bostotte): Inntekt? =
        if (bostotte.utbetalinger.isEmpty()) {
            null
        } else {
            Inntekt(
                type = InntektType.UTBETALING_HUSBANKEN,
                inntektDetaljer =
                    OkonomiDetaljer(
                        detaljer =
                            bostotte.utbetalinger
                                .filter { it.utbetalingsdato.isAfter(LocalDate.now().minusDays(daysToSubtract(bostotte))) }
                                .map { it.toUtbetalingDomain() },
                    ),
            )
        }

    // Dette er fordi søker kan ha fått avslag for en måned grunnet for høy inntekt,
    // men søker har tidligere fått bostøtte og det er forventet at søker får bostøtte neste måned.
    private fun daysToSubtract(bostotte: Bostotte): Long {
        val hasSaker =
            bostotte.saker.any { sak ->
                sak.dato.isAfter(LocalDate.now().minusDays(31))
            }
        val hasUtbetalinger =
            bostotte.utbetalinger.any { utbetaling ->
                utbetaling.utbetalingsdato.isAfter(LocalDate.now().minusDays(31))
            }
        return if (hasSaker || hasUtbetalinger) 31 else 62
    }
}

private fun resolveErrorMessage(e: WebClientResponseException): String? =
    when {
        e.statusCode.is4xxClientError -> "Problemer med å koble opp mot Husbanken!"
        e.statusCode.is5xxServerError -> "Problemer med å hente bostøtte fra Husbanken! Ekstern error: ${e.message}"
        else -> "Problemer med å hente bostøtte informasjon fra Husbanken!"
    }

private fun Sak.toBostotteSak() =
    BostotteSak(
        dato = dato,
        status = BostotteStatus.valueOf(status.name),
        vedtaksstatus = vedtak?.let { Vedtaksstatus.valueOf(vedtak.type) },
        beskrivelse = vedtak?.beskrivelse,
    )

private fun UtbetalingHusbanken.toUtbetalingDomain() =
    Utbetaling(
        netto = belop.toDouble(),
        mottaker = Mottaker.valueOf(mottaker.name),
        utbetalingsdato = utbetalingsdato,
    )

data class HusbankenException(
    val melding: String? = null,
    override val cause: Throwable? = null,
    val soknadId: UUID? = null,
) : SosialhjelpSoknadApiException(melding, cause, soknadId.toString())

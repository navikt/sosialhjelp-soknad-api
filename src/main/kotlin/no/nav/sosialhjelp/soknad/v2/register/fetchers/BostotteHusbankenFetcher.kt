package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Bostotte
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Sak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Utbetaling as UtbetalingHusbanken

// TODO Skal denne være en del av de vanlige fetcherne (interface) ?
@Component
class BostotteHusbankenFetcher(
    private val husbankenClient: HusbankenClient,
) {
    fun fetch(soknadId: UUID): Pair<List<BostotteSak>, Inntekt?> {
        return husbankenClient.hentBostotte(LocalDate.now().minusDays(60), LocalDate.now())
            .toDomain()
            .let { Pair(saveToSaker(it), saveToInntekt(it)) }
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
                                .map { it.toUtbetaling() },
                    ),
            )
        }

    // Dette er fordi søker kan ha fått avslag for en måned grunnet for høy inntekt,
    // men søker har tidligere fått bostøtte og det er forventet at søker får bostøtte neste måned.
    private fun daysToSubtract(bostotte: Bostotte): Long {
        // TODO Nødvendig å logge om saker/utbetalinger er null eller tom
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

private fun Sak.toBostotteSak() =
    BostotteSak(
        dato = dato,
        status = BostotteStatus.valueOf(status.name),
        vedtaksstatus = vedtak?.let { Vedtaksstatus.valueOf(vedtak.type) },
        beskrivelse = vedtak?.beskrivelse,
    )

private fun UtbetalingHusbanken.toUtbetaling() =
    Utbetaling(
        netto = belop.toDouble(),
        mottaker = Mottaker.valueOf(mottaker.name),
        utbetalingsdato = utbetalingsdato,
    )

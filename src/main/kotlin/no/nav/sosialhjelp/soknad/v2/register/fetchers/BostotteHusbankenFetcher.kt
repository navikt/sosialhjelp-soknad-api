package no.nav.sosialhjelp.soknad.v2.register.fetchers

import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Bostotte
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Sak
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.inntekt.husbanken.domain.Utbetaling as UtbetalingHusbanken

@Component
class BostotteHusbankenFetcher(
    private val husbankenClient: HusbankenClient,
    private val okonomiService: OkonomiService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val dokumentasjonService: DokumentasjonService,
) {
    fun fetchAndSave(
        soknadId: UUID,
        token: String,
    ) {
        if (hasBostotteAndSamtykke(soknadId)) {
            okonomiService.removeElementFromOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
            okonomiService.removeBostotteSaker(soknadId)

            getBostotteSaker(soknadId, token)
        }
    }

    private fun getBostotteSaker(
        soknadId: UUID,
        token: String,
    ) {
        husbankenClient.hentBostotte(token, LocalDate.now().minusDays(60), LocalDate.now())
            ?.toDomain()
            ?.let {
                integrasjonStatusService.setStotteHusbankenStatus(soknadId, feilet = false)

                saveToSaker(soknadId, it)
                saveToUtbetalinger(soknadId, it)
            }
            ?: handleHusbankenFetchFailed(soknadId)
    }

    private fun saveToSaker(
        soknadId: UUID,
        bostotte: Bostotte,
    ) {
        bostotte.saker
            .filter { it.dato.isAfter(LocalDate.now().minusDays(daysToSubtract(bostotte))) }
            .map { it.toBostotteSak() }
            .forEach { okonomiService.addBostotteSak(soknadId, it) }
    }

    private fun saveToUtbetalinger(
        soknadId: UUID,
        bostotte: Bostotte,
    ) {
        if (bostotte.utbetalinger.isNotEmpty()) {
            okonomiService.addElementToOkonomi(
                soknadId = soknadId,
                element =
                    Inntekt(
                        type = InntektType.UTBETALING_HUSBANKEN,
                        inntektDetaljer =
                            OkonomiDetaljer(
                                detaljer =
                                    bostotte.utbetalinger
                                        .filter { it.utbetalingsdato.isAfter(LocalDate.now().minusDays(daysToSubtract(bostotte))) }
                                        .map { it.toUtbetaling() },
                            ),
                    ),
            )
        }
    }

    private fun handleHusbankenFetchFailed(soknadId: UUID) {
        integrasjonStatusService.setStotteHusbankenStatus(soknadId, feilet = true)
        okonomiService.addElementToOkonomi(soknadId, InntektType.UTBETALING_HUSBANKEN)
        dokumentasjonService.opprettDokumentasjon(soknadId, InntektType.UTBETALING_HUSBANKEN)
    }

    private fun hasBostotteAndSamtykke(soknadId: UUID): Boolean {
        val bostotte =
            okonomiService.getBekreftelser(soknadId)
                .find { it.type == BekreftelseType.BOSTOTTE }?.verdi == true
        val samtykke =
            okonomiService.getBekreftelser(soknadId)
                .find { it.type == BekreftelseType.BOSTOTTE_SAMTYKKE }?.verdi == true

        return bostotte && samtykke
    }

    // Dette er fordi søker kan ha fått avslag for en måned grunnet for høy inntekt,
    // men søker har tidligere fått bostøtte og det er forventet at søker får bostøtte neste måned.
    private fun daysToSubtract(bostotte: Bostotte): Long {
        // TODO Nødvendig å logge om saker/utbetalinger er null eller tom
        val hasSaker =
            bostotte.saker.any {
                    sak ->
                sak.dato.isAfter(LocalDate.now().minusDays(30))
            }
        val hasUtbetalinger =
            bostotte.utbetalinger.any {
                    utbetaling ->
                utbetaling.utbetalingsdato.isAfter(LocalDate.now().minusDays(30))
            }
        return if (hasSaker || hasUtbetalinger) 30 else 60
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

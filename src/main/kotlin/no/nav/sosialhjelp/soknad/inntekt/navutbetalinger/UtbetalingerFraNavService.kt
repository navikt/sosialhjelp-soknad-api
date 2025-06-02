package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.UtbetalingerFraNavService.Companion.ORGNR_NAV
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelse
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelseskomponent
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UtbetalingerFraNavService(
    private val navUtbetalingerClient: UtbetalingerFraNavClient,
    private val orgService: OrganisasjonService,
) {
    fun getUtbetalingerSiste40Dager(personId: String): List<UtbetalingMedKomponent>? {
        return navUtbetalingerClient.getUtbetalingerSiste40Dager(personId)
            ?.toUtbetalingMedKomponent(orgNavn)
            ?.also { utbetalinger ->

                val duplicates =
                    utbetalinger.groupBy {
                        listOf(it.utbetaling.tittel, it.utbetaling.netto, it.utbetaling.brutto, it.utbetaling.utbetalingsdato)
                    }.filter { it.value.size > 1 }

                val totalDuplicatesCount = duplicates.values.sumOf { it.size }

                if (totalDuplicatesCount > 0) {
                    logger.info("Ut av ${utbetalinger.size} utbetaling(er) så er det $totalDuplicatesCount som er identiske utbetaling(er)")
                }

                logger.info("Antall navytelser utbetaling: ${utbetalinger.size}. ${utbetalinger.komponenterLogg()}")
            }
    }

    private fun UtbetalDataDto.toUtbetalingMedKomponent(orgNavn: String): List<UtbetalingMedKomponent>? {
        if (feilet || utbetalinger == null) return null

        return utbetalinger
            .filter { it.utbetalingsdato != null }
            .filter { it.utbetalingsdato?.utbetaltSiste40Dager() ?: false }
            .flatMap { utbetaling ->
                utbetaling.ytelseListe
                    .filter { it.isUtbetaltBruker(utbetaling.utbetaltTil?.navn) }
                    .map { it.toUtbetalingMedKomponent(utbetaling.utbetalingsdato, orgNavn) }
            }
            // TODO Ekstra logging
            .also { if (utbetalinger.isNotEmpty()) logYtelser(utbetalinger, it) }
    }

    // TODO Ekstra logging
    private fun logYtelser(
        utbetalinger: List<no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling>,
        utbetalingMedKomponents: List<UtbetalingMedKomponent>,
    ) {
        OversiktUtbetalinger(
            utbetalinger.size,
            utbetalinger.flatMap { it.ytelseListe }.size,
            utbetalingMedKomponents.size,
        )
            .also { logger.info("Oversikt Utbetalinger fra Nav: $it") }
    }

    // TODO Ekstra logging
    private data class OversiktUtbetalinger(
        val antallUtbetalingerDto: Int,
        val antallYtelserDto: Int,
        val antallUtbetalingerMedKomponent: Int,
    )

    private val orgNavn get() = orgService.hentOrgNavn(ORGNR_NAV)

    companion object {
        private val logger by logger()
        const val ORGNR_NAV = "889640782"
    }
}

private fun Ytelse.toUtbetalingMedKomponent(
    utbetalingsdato: LocalDate?,
    orgNavn: String,
) =
    UtbetalingMedKomponent(
        tittel = ytelsestype ?: "",
        utbetaling =
            Utbetaling(
                belop = ytelseNettobeloep.toDouble(),
                brutto = ytelseskomponentersum.toDouble(),
                netto = ytelseNettobeloep.toDouble(),
                skattetrekk = skattsum.toDouble(),
                andreTrekk = trekksum.toDouble(),
                utbetalingsdato = utbetalingsdato,
                periodeFom = ytelsesperiode.fom,
                periodeTom = ytelsesperiode.tom,
                tittel = ytelsestype ?: "",
                organisasjon = Organisasjon(orgNavn, ORGNR_NAV),
            ),
        komponenter = ytelseskomponentListe?.map { it.toKomponent() } ?: emptyList(),
    )

private fun Ytelseskomponent.toKomponent() =
    Komponent(
        type = ytelseskomponenttype,
        belop = ytelseskomponentbeloep?.toDouble(),
        satsType = satstype,
        satsBelop = satsbeloep?.toDouble(),
        satsAntall = satsantall,
    )

private fun LocalDate.utbetaltSiste40Dager() = isAfter(LocalDate.now().minusDays(40))

private fun Ytelse.isUtbetaltBruker(utbetaltTil: String?): Boolean {
    return if (rettighetshaver.navn == null || utbetaltTil == null) {
        false
    } else {
        rettighetshaver.navn.trim().equals(utbetaltTil.trim(), ignoreCase = true)
    }
}

private fun List<UtbetalingMedKomponent>.komponenterLogg(): String {
    return when (isEmpty()) {
        true -> ""
        false ->
            joinToString(prefix = "Antall komponenter: ", separator = ", ") {
                "Utbetaling ${indexOf(it)} -> ${it.komponenter.size}"
            }
    }
}

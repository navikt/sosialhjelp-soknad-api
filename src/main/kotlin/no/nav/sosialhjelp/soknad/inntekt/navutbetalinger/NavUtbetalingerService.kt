package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelse
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelseskomponent
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
open class NavUtbetalingerService(
    private val navUtbetalingerClient: NavUtbetalingerClient
) {

    open fun getUtbetalingerSiste40Dager(ident: String): List<NavUtbetaling>? {
        val utbetalinger: List<NavUtbetaling>

        val utbetalDataDto: UtbetalDataDto? = navUtbetalingerClient.getUtbetalingerSiste40Dager(ident)
        if (utbetalDataDto == null || utbetalDataDto.feilet || utbetalDataDto.utbetalinger == null) {
            return null
        }

        utbetalinger = mapToNavutbetalinger(utbetalDataDto)

        log.info("Antall navytelser utbetaling: ${utbetalinger.size}. ${komponenterLogg(utbetalinger)}")

        return utbetalinger
    }

    private fun komponenterLogg(utbetalinger: List<NavUtbetaling>): String {
        if (utbetalinger.isEmpty()) {
            return ""
        }
        return utbetalinger.joinToString(
            prefix = "Antall komponenter: ",
            separator = ", "
        ) { "Utbetaling${utbetalinger.indexOf(it)} - ${it.komponenter.size}" }
    }

    companion object {
        private val log by logger()
        private const val NAVYTELSE = "navytelse"
        private const val ORGNR_NAV = "889640782"

        private fun mapToNavutbetalinger(utbetalDataDto: UtbetalDataDto?): List<NavUtbetaling> {
            if (utbetalDataDto?.utbetalinger == null) {
                return emptyList()
            }

            return utbetalDataDto.utbetalinger
                .filter { it.utbetalingsdato != null }
                .filter { utbetaltSiste40Dager(it.utbetalingsdato) }
                .flatMap { utbetaling ->
                    utbetaling.ytelseListe
                        .filter { utbetaltTilBruker(it, utbetaling) }
                        .map {
                            NavUtbetaling(
                                type = NAVYTELSE,
                                netto = it.ytelseNettobeloep.toDouble(),
                                brutto = it.ytelseskomponentersum.toDouble(),
                                skattetrekk = it.skattsum.toDouble(),
                                andreTrekk = it.trekksum.toDouble(),
                                bilagsnummer = it.bilagsnummer,
                                utbetalingsdato = utbetaling.utbetalingsdato,
                                periodeFom = it.ytelsesperiode.fom,
                                periodeTom = it.ytelsesperiode.tom,
                                komponenter = mapToKomponenter(it.ytelseskomponentListe),
                                tittel = it.ytelsestype ?: "",
                                orgnummer = ORGNR_NAV
                            )
                        }
                }
        }

        private fun utbetaltSiste40Dager(utbetalingsdato: LocalDate?): Boolean {
            return if (utbetalingsdato != null) !utbetalingsdato.isBefore(LocalDate.now().minusDays(40)) else false
        }

        private fun utbetaltTilBruker(ytelse: Ytelse, utbetaling: Utbetaling): Boolean {
            val utbetaltTil = utbetaling.utbetaltTil?.navn
            val rettighetshaver = ytelse.rettighetshaver

            if (utbetaltTil.isNullOrEmpty()) {
                return false
            }

            val navn = rettighetshaver.navn
            if (navn.isNullOrEmpty()) {
                return false
            }

            return utbetaltTil.trim().equals(navn.trim(), ignoreCase = true)
        }

        private fun mapToKomponenter(ytelseskomponentList: List<Ytelseskomponent>?): List<Komponent> {
            if (ytelseskomponentList == null) {
                return emptyList()
            }
            log.info("Antall navytelser komponent {}", ytelseskomponentList.size)
            return ytelseskomponentList
                .map {
                    Komponent(
                        type = it.ytelseskomponenttype,
                        belop = it.ytelseskomponentbeloep?.toDouble(),
                        satsType = it.satstype,
                        satsBelop = it.satsbeloep?.toDouble(),
                        satsAntall = it.satsantall
                    )
                }
        }
    }
}

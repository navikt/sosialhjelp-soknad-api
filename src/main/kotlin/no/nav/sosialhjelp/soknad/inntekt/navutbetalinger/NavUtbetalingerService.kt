package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.NavUtbetalingerDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Utbetaling
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelse
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.Ytelseskomponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.toDomain
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
open class NavUtbetalingerService(
    private val navUtbetalingerClient: NavUtbetalingerClient,
    private val unleash: Unleash
) {

    open fun getUtbetalingerSiste40Dager(ident: String): List<NavUtbetaling>? {
        val utbetalinger: List<NavUtbetaling>

        if (unleash.isEnabled(BRUK_UTBETALDATATJENESTE_ENABLED, true)) {
            log.info("Bruk av ny utbetaldatatjeneste er enablet og denne benyttes")
            val utbetalDataDto: UtbetalDataDto? = navUtbetalingerClient.getUtbetalingerSiste40Dager(ident)
            if (utbetalDataDto == null || utbetalDataDto.feilet || utbetalDataDto.utbetalinger == null) {
                return null
            }

            utbetalinger = mapToNavutbetalinger(utbetalDataDto)
        } else {
            log.info("Bruk av ny utbetaldatatjeneste er ikke enablet og gammel utbetalingstjeneste benyttes")
            val navUtbetalingerDto: NavUtbetalingerDto? = navUtbetalingerClient.getUtbetalingerSiste40DagerLegacy(ident)
            if (navUtbetalingerDto == null || navUtbetalingerDto.feilet || navUtbetalingerDto.utbetalinger == null) {
                return null
            }

            utbetalinger = navUtbetalingerDto.utbetalinger.map { it.toDomain }
            sammenlignMedSkyggeproduksjon(utbetalinger, ident)
        }
        log.info("Antall navytelser utbetaling ${utbetalinger.size}. ${komponenterLogg(utbetalinger)}")

        return utbetalinger
    }

    // Dette kallet fjernes når vi har kontroll og har gått over til ny tjeneste. Benyttes da det er vanskelig å få
// testdata for utbetaling i Q1
    private fun sammenlignMedSkyggeproduksjon(utbetalinger: List<NavUtbetaling>, ident: String) {
        log.info("Gjør kall til ny utbetal tjeneste - skyggeproduksjon")

        val utbetalDataDto: UtbetalDataDto? = navUtbetalingerClient.getUtbetalingerSiste40Dager(ident)
        var utbetalingOgUtbetalDataErLike = false

        if (utbetalDataDto == null || utbetalDataDto.feilet || utbetalDataDto.utbetalinger == null) {
            log.info("Klarte ikke hente noe data fra ny utbetaltjeneste - skyggeproduksjon")
        } else {
            val utbetalingerSkygge = mapToNavutbetalinger(utbetalDataDto).sortedBy { it.utbetalingsdato }
            val utbetalingerSortert = utbetalinger.sortedBy { it.utbetalingsdato }

            if (utbetalingerSkygge.isEmpty() && utbetalingerSortert.isEmpty()) {
                utbetalingOgUtbetalDataErLike = true
            }

            val utbetalingStringBuilder = StringBuilder()
            val utbetalingSkyggeStringBuilder = StringBuilder()
            if (utbetalingerSortert.isNotEmpty() && utbetalinger.size == utbetalingerSkygge.size) {
                utbetalingerSkygge.forEachIndexed { index, utbetalingSkygge ->
                    val utbetaling = utbetalingerSortert.get(index)
                    if (utbetaling.utbetalingsdato == utbetalingSkygge.utbetalingsdato &&
                        utbetaling.tittel.trim() == utbetalingSkygge.tittel.trim() &&
                        utbetaling.netto == utbetalingSkygge.netto
                    ) {
                        utbetalingOgUtbetalDataErLike = true
                    } else {
                        utbetalingStringBuilder.append("Utbetaling nr: ").append(index + 1)
                            .append(" Utbetalingsdato: ").append(utbetaling.utbetalingsdato)
                            .append(" Tittel: ").append(utbetaling.tittel)
                            .append(" Netto: ").appendLine(utbetaling.netto)

                        utbetalingSkyggeStringBuilder.append("Utbetaling nr: ").append(index + 1)
                            .append(" Utbetalingsdato: ").append(utbetalingSkygge.utbetalingsdato)
                            .append(" Tittel: ").append(utbetalingSkygge.tittel)
                            .append(" Netto: ").appendLine(utbetalingSkygge.netto)
                    }
                }
            }

            if (utbetalingOgUtbetalDataErLike) {
                log.info("Utbetaldata Skyggeproduksjon - Nav utbetalingsdata som vi bruker er like fra gammel Utbetalingv1 tjeneste og ny UtbetalData tjeneste")
            } else {
                log.info(
                    """UtbetalData skyggeproduksjon - Data hentet fra UtbetalingV1 og UtbetalData tjeneste er forskjellige.
                         UtbetalingV1 returnerte ${utbetalinger.size} utbetalinger og UtbetalData returnerte ${utbetalingerSkygge.size} utbetalinger.
                            Utbetalinger fra Utbetalingv1: $utbetalingStringBuilder
                            Utbetalinger fra Utbetaldata: $utbetalingSkyggeStringBuilder
                            Hele respons fra Utbetalingv1: $utbetalinger
                            Hele respons fra UtbetalData: $utbetalingerSkygge
                    """.trimIndent()
                )
            }
        }
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
        const val BRUK_UTBETALDATATJENESTE_ENABLED = "sosialhjelp.soknad.bruk_sokos_utbetaldata_tjeneste"

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

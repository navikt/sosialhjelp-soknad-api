package no.nav.sosialhjelp.soknad.navutbetalinger.dto

import no.nav.sosialhjelp.soknad.navutbetalinger.domain.Komponent
import no.nav.sosialhjelp.soknad.navutbetalinger.domain.NavUtbetaling
import java.time.LocalDate

data class NavUtbetalingerDto(
    val utbetalinger: List<NavUtbetalingDto>?,
    val feilet: Boolean
)

data class NavUtbetalingDto(
    val type: String,
    val netto: Double?,
    val brutto: Double?,
    val skattetrekk: Double?,
    val andreTrekk: Double?,
    val bilagsnummer: String?,
    val utbetalingsdato: LocalDate?,
    val periodeFom: LocalDate?,
    val periodeTom: LocalDate?,
    val komponenter: List<KomponentDto>,
    val tittel: String,
    val orgnummer: String
)

data class KomponentDto(
    val type: String?,
    val belop: Double?,
    val satsType: String?,
    val satsBelop: Double?,
    val satsAntall: Double?
)

val NavUtbetalingDto.toDomain: NavUtbetaling
    get() {
        return NavUtbetaling(
            type = type,
            netto = netto,
            brutto = brutto,
            skattetrekk = skattetrekk,
            andreTrekk = andreTrekk,
            bilagsnummer = bilagsnummer,
            utbetalingsdato = utbetalingsdato,
            periodeFom = periodeFom,
            periodeTom = periodeTom,
            komponenter = komponenter.map { it.toDomain },
            tittel = tittel,
            orgnummer = orgnummer
        )
    }

val KomponentDto.toDomain: Komponent
    get() {
        return Komponent(
            type = type,
            belop = belop,
            satsType = satsType,
            satsBelop = satsBelop,
            satsAntall = satsAntall
        )
    }

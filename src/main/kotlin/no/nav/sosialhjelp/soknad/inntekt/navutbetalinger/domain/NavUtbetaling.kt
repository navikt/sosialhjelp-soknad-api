package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain

import java.time.LocalDate

data class NavUtbetaling(
    val type: String,
    val netto: Double?,
    val brutto: Double?,
    val skattetrekk: Double?,
    val andreTrekk: Double?,
    val bilagsnummer: String?,
    val utbetalingsdato: LocalDate?,
    val periodeFom: LocalDate?,
    val periodeTom: LocalDate?,
    val komponenter: List<Komponent>,
    val tittel: String,
    val orgnummer: String,
)

data class Komponent(
    val type: String?,
    val belop: Double?,
    val satsType: String?,
    val satsBelop: Double?,
    val satsAntall: Double?,
)

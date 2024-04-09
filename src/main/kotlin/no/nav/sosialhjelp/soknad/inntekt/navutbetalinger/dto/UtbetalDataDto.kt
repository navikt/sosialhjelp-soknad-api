package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto

import java.math.BigDecimal
import java.time.LocalDate

data class UtbetalDataDto(
    val utbetalinger: List<Utbetaling>?,
    val feilet: Boolean
)

data class Utbetaling(
    val posteringsdato: LocalDate?,
    val utbetaltTil: Aktoer?,
    val utbetalingNettobeloep: BigDecimal?,
    val utbetalingsdato: LocalDate?,
    val forfallsdato: LocalDate?,
    val utbetalingsmelding: String?,
    val utbetaltTilKonto: Bankkonto?,
    val utbetalingsmetode: String?,
    val utbetalingsstatus: String?,
    val ytelseListe: List<Ytelse> = emptyList()
)

data class Ytelse(
    val ytelsestype: String?,
    val ytelsesperiode: Periode,
    val ytelseNettobeloep: BigDecimal,
    val rettighetshaver: Aktoer,
    val skattsum: BigDecimal,
    val trekksum: BigDecimal,
    val ytelseskomponentersum: BigDecimal,
    val skattListe: List<Skatt>? = null,
    val trekkListe: List<Trekk>? = null,
    val ytelseskomponentListe: List<Ytelseskomponent>? = null,
    val bilagsnummer: String?,
    val refundertForOrg: Aktoer?
)

data class Aktoer(
    val aktoertype: Aktoertype,
    val ident: String,
    val navn: String?
)

data class Ytelseskomponent(
    val ytelseskomponenttype: String?,
    val satsbeloep: BigDecimal?,
    val satstype: String?,
    val satsantall: Double?,
    val ytelseskomponentbeloep: BigDecimal?
)

data class Skatt(val skattebeloep: BigDecimal?)

data class Trekk(
    val trekktype: String?,
    val trekkbeloep: BigDecimal?,
    val kreditor: String?
)

data class Bankkonto(
    val kontonummer: String,
    val kontotype: String
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)

enum class Aktoertype {
    PERSON,
    ORGANISASJON,
    SAMHANDLER
}

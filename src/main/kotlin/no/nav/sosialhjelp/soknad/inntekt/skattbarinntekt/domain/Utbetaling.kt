package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain

import java.time.LocalDate

data class Utbetaling(
    val type: String?,
    var brutto: Double,
    var skattetrekk: Double,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate,
    var tittel: String,
    val orgnummer: String
)

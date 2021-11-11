package no.nav.sosialhjelp.soknad.skattbarinntekt.domain

import java.time.LocalDate

data class Utbetaling(
    val type: String?,
    var brutto: Double,
    var skattetrekk: Double,
    val periodeFom: LocalDate?,
    val periodeTom: LocalDate?,
    var tittel: String?,
    val orgnummer: String
)

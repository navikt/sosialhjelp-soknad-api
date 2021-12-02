package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto

import java.time.LocalDate

data class Sokedata(
    val identifikator: String,
    val fom: LocalDate,
    val tom: LocalDate
)

package no.nav.sosialhjelp.soknad.client.skatteetaten.dto

import java.time.LocalDate

data class Sokedata(
    val identifikator: String,
    val fom: LocalDate,
    val tom: LocalDate
)

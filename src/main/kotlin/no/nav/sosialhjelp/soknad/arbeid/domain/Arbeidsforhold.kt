package no.nav.sosialhjelp.soknad.arbeid.domain

import java.time.LocalDate

data class Arbeidsforhold(
    val orgnr: String?,
    val arbeidsgivernavn: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val fastStillingsprosent: Long? = 0L,
    val harFastStilling: Boolean?,
)

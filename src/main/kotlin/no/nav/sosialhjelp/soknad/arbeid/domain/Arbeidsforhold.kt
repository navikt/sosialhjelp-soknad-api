package no.nav.sosialhjelp.soknad.arbeid.domain

data class Arbeidsforhold(
    val orgnr: String?,
    val arbeidsgivernavn: String,
    val fom: String?,
    val tom: String?,
    val fastStillingsprosent: Long? = 0L,
    val harFastStilling: Boolean?,
)

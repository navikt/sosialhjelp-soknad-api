package no.nav.sosialhjelp.soknad.arbeid.domain

@Deprecated("Erstattet med no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold")
data class Arbeidsforhold(
    val orgnr: String?,
    val arbeidsgivernavn: String,
    val fom: String?,
    val tom: String?,
    val fastStillingsprosent: Long? = 0L,
    val harFastStilling: Boolean?
)

fun Arbeidsforhold.toV2Arbeidsforhold(): no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold {
    return no.nav.sosialhjelp.soknad.v2.soknad.Arbeidsforhold(
        orgnummer = this.orgnr,
        arbeidsgivernavn = this.arbeidsgivernavn,
        start = this.fom,
        slutt = this.tom,
        fastStillingsprosent = this.fastStillingsprosent?.toInt(),
        harFastStilling = this.harFastStilling,
    )
}

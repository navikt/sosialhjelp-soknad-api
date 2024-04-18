package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto

data class NavUtbetalingerRequest(
    val ident: String,
    val rolle: String,
    val periode: Periode,
    val periodetype: String,
)

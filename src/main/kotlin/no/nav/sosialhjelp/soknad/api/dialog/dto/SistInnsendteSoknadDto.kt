package no.nav.sosialhjelp.soknad.api.dialog.dto

data class SistInnsendteSoknadDto(
    val ident: String,
    val navEnhet: String,
    val innsendtDato: String
)

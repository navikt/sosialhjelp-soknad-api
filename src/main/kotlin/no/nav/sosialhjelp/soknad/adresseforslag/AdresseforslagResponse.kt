package no.nav.sosialhjelp.soknad.adresseforslag

import no.nav.sosialhjelp.soknad.pdl.types.CompletionAdresse

data class AdresseforslagResponse(
    val suggestions: List<String>,
    val addressFound: CompletionAdresse?
)

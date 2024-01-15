package no.nav.sosialhjelp.soknad.app.client.pdl

data class PdlRequest(
    val query: String,
    val variables: Map<String, Any>,
)

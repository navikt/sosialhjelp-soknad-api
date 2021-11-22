package no.nav.sosialhjelp.soknad.client.pdl

data class PdlRequest(
    val query: String,
    val variables: Map<String, Any>
)

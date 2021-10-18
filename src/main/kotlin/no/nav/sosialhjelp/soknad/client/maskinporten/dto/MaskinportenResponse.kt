package no.nav.sosialhjelp.soknad.client.maskinporten.dto

data class MaskinportenResponse(
    val access_token: String,
    val token_type: String?,
    val expires_in: Int?,
    val scope: String?
)
package no.nav.sosialhjelp.soknad.client.maskinporten

data class MaskinportenConfig(
    val clientId: String,
    val jwkPrivate: String,
    val scope: String,
    val wellKnownUrl: String
)

package no.nav.sosialhjelp.soknad.auth.maskinporten

data class MaskinportenProperties(
    val clientId: String,
    val jwkPrivate: String,
    val scope: String,
    val wellKnownUrl: String
)

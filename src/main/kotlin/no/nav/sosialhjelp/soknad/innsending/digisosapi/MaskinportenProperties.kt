package no.nav.sosialhjelp.soknad.innsending.digisosapi

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "maskinporten.v2")
data class MaskinportenProperties(
    val endpoint: String,
    val clientId: String,
    val clientJwk: String,
    val issuer: String,
)

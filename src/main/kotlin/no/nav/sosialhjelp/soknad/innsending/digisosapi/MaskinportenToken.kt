package no.nav.sosialhjelp.soknad.innsending.digisosapi

import java.io.Serializable

data class MaskinportenToken(
    val access_token: String,
    val expires_in: Long,
) : Serializable

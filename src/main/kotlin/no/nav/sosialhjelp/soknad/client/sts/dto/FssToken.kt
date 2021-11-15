package no.nav.sosialhjelp.soknad.client.sts.dto

import java.time.LocalDateTime

data class FssToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Long
) {

    fun getExpirationTime(): LocalDateTime = LocalDateTime.now().plusSeconds(expires_in - 10L)
}

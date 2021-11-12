package no.nav.sosialhjelp.soknad.client.sts.dto

import java.time.LocalDateTime

data class FssToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Long
) {
    private val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expires_in - 60L)
    val isExpired: Boolean = expirationTime.isBefore(LocalDateTime.now())
}

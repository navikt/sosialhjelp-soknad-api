package no.nav.sosialhjelp.soknad.client.sts.dto

import java.time.LocalDateTime

data class FssToken(
    val access_token: String,
    val token_type: String,
    val expires_in: Long
) {

    val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expires_in - 10L)

    companion object {
        fun shouldRenewToken(token: FssToken?): Boolean {
            if (token == null) {
                return true
            }
            return isExpired(token)
        }

        private fun isExpired(token: FssToken): Boolean {
            return token.expirationTime.isBefore(LocalDateTime.now())
        }
    }
}

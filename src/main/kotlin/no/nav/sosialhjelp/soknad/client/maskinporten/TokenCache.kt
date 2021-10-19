package no.nav.sosialhjelp.soknad.client.maskinporten

import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class TokenCache(
    private val token: String? = null
) {
    fun getToken(): SignedJWT? {
        val signedJWT: SignedJWT? = token?.let { SignedJWT.parse(it) }
        return signedJWT?.takeUnless { isExpired(it) }
    }

    private fun isExpired(jwt: SignedJWT): Boolean {
        return jwt.jwtClaimsSet?.expirationTime
            ?.toLocalDateTime?.minusSeconds(TJUE_SEKUNDER)?.isBefore(LocalDateTime.now())
            ?: true
    }

    private val Date.toLocalDateTime: LocalDateTime?
        get() = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime()

    companion object {
        private const val TJUE_SEKUNDER: Long = 20
    }
}

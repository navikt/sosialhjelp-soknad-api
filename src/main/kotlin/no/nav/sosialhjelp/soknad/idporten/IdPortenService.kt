package no.nav.sosialhjelp.soknad.idporten

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.idporten.client.AccessToken
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import no.nav.sosialhjelp.soknad.idporten.IdPortenServiceImpl.CachedToken.Companion.shouldRenewToken
import org.springframework.stereotype.Component
import java.time.LocalDateTime

interface IdPortenService {
    fun getToken(): AccessToken
}

@Component
class IdPortenServiceImpl(
    private val idPortenClient: IdPortenClient
) : IdPortenService {

    private var cachedToken: CachedToken? = null

    override fun getToken(): AccessToken {
        if (shouldRenewToken(cachedToken)) {
            val tidspunktForHenting: LocalDateTime = LocalDateTime.now()
            return runBlocking(Dispatchers.IO) { idPortenClient.requestToken() }
                .also { cachedToken = CachedToken(it, tidspunktForHenting) }
        }

        return cachedToken!!.accessToken
    }

    private data class CachedToken(
        val accessToken: AccessToken,
        val created: LocalDateTime
    ) {
        // 10 sek buffer fra expiresIn
        val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(accessToken.expiresIn - 10L)

        companion object {
            fun shouldRenewToken(token: CachedToken?): Boolean {
                if (token == null) {
                    return true
                }
                return isExpired(token)
            }

            private fun isExpired(token: CachedToken): Boolean {
                return token.expirationTime.isBefore(LocalDateTime.now())
            }
        }
    }
}

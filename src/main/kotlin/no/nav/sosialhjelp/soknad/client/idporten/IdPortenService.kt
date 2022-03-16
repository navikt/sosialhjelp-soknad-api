package no.nav.sosialhjelp.soknad.client.idporten

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.idporten.client.AccessToken
import no.nav.sosialhjelp.idporten.client.IdPortenClient
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
        val token = cachedToken
        if (token == null || shouldRenewToken(token)) {
            val tidspunktForHenting: LocalDateTime = LocalDateTime.now()
            return runBlocking(Dispatchers.IO) { idPortenClient.requestToken() }
                .also { cachedToken = CachedToken(it, tidspunktForHenting) }
        }
        return token.accessToken
    }

    private fun shouldRenewToken(token: CachedToken): Boolean {
        return token.isExpired()
    }

    private data class CachedToken(
        val accessToken: AccessToken,
        val created: LocalDateTime
    ) {
        // 10 sek buffer fra expiresIn
        val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(accessToken.expiresIn - 10L)

        fun isExpired(): Boolean {
            return expirationTime.isBefore(LocalDateTime.now())
        }
    }
}

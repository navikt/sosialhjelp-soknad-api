package no.nav.sosialhjelp.soknad.auth.tokenx

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils.isNonProduction
import no.nav.sosialhjelp.soknad.redis.RedisService
import no.nav.sosialhjelp.soknad.redis.TOKENDINGS_CACHE_KEY_PREFIX
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Instant
import java.util.Date
import java.util.UUID

class TokendingsService(
    private val tokendingsClient: TokendingsClient,
    private val tokendingsClientId: String,
    tokendingsPrivateJwk: String,
    private val redisService: RedisService
) {

    private val privateRsaKey: RSAKey = if (tokendingsPrivateJwk == "generateRSA") {
        if (!isNonProduction()) throw RuntimeException("Generation of RSA keys is not allowed in prod.")
        RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID(UUID.randomUUID().toString()).generate()
    } else {
        RSAKey.parse(tokendingsPrivateJwk)
    }

    suspend fun exchangeToken(subject: String, token: String, audience: String): String {
        hentFraCache("$audience-$subject")?.let { return it }

        val jwt = createSignedAssertion(tokendingsClientId, tokendingsClient.audience, privateRsaKey)

        return try {
            tokendingsClient.exchangeToken(token, jwt, audience).accessToken
                .also { lagreTilCache("$audience-$subject", it) }
        } catch (e: WebClientResponseException) {
            log.warn("Error message from server: ${e.responseBodyAsString}")
            throw e
        }
    }

    private fun hentFraCache(key: String): String? {
        return redisService.getString(TOKENDINGS_CACHE_KEY_PREFIX + key)
    }

    private fun lagreTilCache(key: String, onBehalfToken: String) {
        redisService.setex(TOKENDINGS_CACHE_KEY_PREFIX + key, onBehalfToken.toByteArray(), 30)
    }

    private fun createSignedAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
        val now = Instant.now()
        return JWT.create()
            .withSubject(clientId)
            .withIssuer(clientId)
            .withAudience(audience)
            .withIssuedAt(Date.from(now))
            .withNotBefore(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(60)))
            .withJWTId(UUID.randomUUID().toString())
            .withKeyId(rsaKey.keyID)
            .sign(Algorithm.RSA256(null, rsaKey.toRSAPrivateKey()))
    }

    companion object {
        private val log by logger()
    }
}

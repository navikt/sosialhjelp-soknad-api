package no.nav.sosialhjelp.soknad.auth.azure

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.redis.AZURE_SYSTEM_TOKEN
import no.nav.sosialhjelp.soknad.redis.CACHE_30_SECONDS
import no.nav.sosialhjelp.soknad.redis.RedisService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class AzureadService(
    private val azureClient: AzureadClient,
    private val redisService: RedisService,
) {
    suspend fun getSystemToken(scope: String): String {
        redisService.getString("$AZURE_SYSTEM_TOKEN$scope")?.let { return it }

        return try {
            azureClient.getSystemToken(scope).accessToken
                .also { lagreTilCache(scope, it) }
        } catch (e: WebClientResponseException) {
            log.warn("Error message from server: ${e.responseBodyAsString}")
            throw e
        }
    }

    private fun lagreTilCache(
        scope: String,
        accessToken: String,
    ) {
        redisService.setex("$AZURE_SYSTEM_TOKEN$scope", accessToken.toByteArray(), CACHE_30_SECONDS)
    }

    companion object {
        private val log by logger()
    }
}

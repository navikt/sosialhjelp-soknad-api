package no.nav.sosialhjelp.soknad.client.azure

import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.client.redis.AZURE_SYSTEM_TOKEN
import no.nav.sosialhjelp.soknad.client.redis.CACHE_30_SECONDS
import no.nav.sosialhjelp.soknad.client.redis.RedisService
import org.springframework.web.reactive.function.client.WebClientResponseException

class AzureadService(
    private val azureClient: AzureadClient,
    private val azureClientId: String,
    private val redisService: RedisService
) {

    suspend fun getSystemToken(scope: String): String {
        redisService.getString("$AZURE_SYSTEM_TOKEN$scope")?.let { return it }

        return try {
            azureClient.getSystemToken(azureClientId, scope).accessToken
                .also { lagreTilCache(scope, it) }
        } catch (e: WebClientResponseException) {
            log.warn("Error message from server: ${e.responseBodyAsString}")
            throw e
        }
    }

    private fun lagreTilCache(scope: String, accessToken: String) {
        redisService.setex("$AZURE_SYSTEM_TOKEN$scope", accessToken.toByteArray(), CACHE_30_SECONDS)
    }

    companion object {
        private val log by logger()
    }
}

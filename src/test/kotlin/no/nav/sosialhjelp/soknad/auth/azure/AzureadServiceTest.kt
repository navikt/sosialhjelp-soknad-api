package no.nav.sosialhjelp.soknad.auth.azure

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.redis.RedisService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AzureadServiceTest {
    private val azureClient: AzureadClient = mockk()
    private val redisService: RedisService = mockk()

    private val azureadService = AzureadService(azureClient, redisService)

    private val scope = "scope"
    private val systemtoken = "systemtoken"

    @Test
    internal fun `get systemtoken fra cache`() {
        runBlocking {
            every { redisService.getString(any()) } returns systemtoken

            val token = azureadService.getSystemToken(scope)
            assertThat(token).isEqualTo(systemtoken)

            coVerify { azureClient wasNot Called }
        }
    }

    @Test
    internal fun `get systemtoken fra client`() {
        runBlocking {
            every { redisService.getString(any()) } returns null
            every { redisService.setex(any(), any(), any()) } just runs

            val mockToken: AzureadTokenResponse = mockk()
            every { mockToken.accessToken } returns systemtoken
            coEvery { azureClient.getSystemToken(any()) } returns mockToken

            val token = azureadService.getSystemToken(scope)
            assertThat(token).isEqualTo(systemtoken)
        }
    }
}

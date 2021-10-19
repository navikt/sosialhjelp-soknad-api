package no.nav.sosialhjelp.soknad.client.idporten

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import no.nav.sosialhjelp.idporten.client.AccessToken
import no.nav.sosialhjelp.idporten.client.IdPortenClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IdPortenServiceImplTest {

    private val idPortenClient: IdPortenClient = mockk()

    private val idPortenService = IdPortenServiceImpl(idPortenClient)

    private val accessToken = AccessToken("token", 99)
    private val nextAccessToken = AccessToken("token2", 100)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    internal fun `henter token fra client og cacher den`() {
        coEvery { idPortenClient.requestToken() } returns accessToken
        val result = idPortenService.getToken()
        assertThat(result).isEqualTo(accessToken)
        coVerify(exactly = 1) { idPortenClient.requestToken() }

        coEvery { idPortenClient.requestToken() } returns nextAccessToken
        val nextResult = idPortenService.getToken()
        assertThat(nextResult).isEqualTo(accessToken)
        coVerify(exactly = 1) { idPortenClient.requestToken() }
    }

    @Test
    internal fun `expired token erstattes av nytt`() {
        val tokenToBeReplaced = AccessToken("asd", 0)
        coEvery { idPortenClient.requestToken() } returns tokenToBeReplaced
        val result = idPortenService.getToken()
        assertThat(result).isEqualTo(tokenToBeReplaced)
        coVerify(exactly = 1) { idPortenClient.requestToken() }

        coEvery { idPortenClient.requestToken() } returns nextAccessToken
        val nextResult = idPortenService.getToken()
        assertThat(nextResult).isEqualTo(nextAccessToken)
        coVerify(exactly = 2) { idPortenClient.requestToken() }
    }
}

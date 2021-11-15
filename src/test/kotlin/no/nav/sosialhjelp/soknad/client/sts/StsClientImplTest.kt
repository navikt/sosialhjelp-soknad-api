package no.nav.sosialhjelp.soknad.client.sts

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.soknad.client.sts.dto.FssToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget

internal class StsClientImplTest {

    private val client: Client = mockk()
    private val stsClient = StsClientImpl(client, "baseurl")

    private val webTarget: WebTarget = mockk()
    private val request: Invocation.Builder = mockk()

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        every { client.target(any<String>()) } returns webTarget
        every { webTarget.queryParam(any(), any()) } returns webTarget
        every { webTarget.request() } returns request
        every { request.get(FssToken::class.java) } returns FssToken("asdf", "type", 3600L)
    }

    @Test
    internal fun skalHenteTokenFraClientHvisCacheErTom() {
        val fssToken = stsClient.getFssToken()

        assertThat(fssToken.access_token).isEqualTo("asdf")
        assertThat(fssToken.token_type).isEqualTo("type")
        assertThat(fssToken.expires_in).isEqualTo(3600L)
    }

    @Test
    internal fun toPafolgendeKallSkalSetteCache() {
        val first = stsClient.getFssToken()

        assertThat(first.access_token).isEqualTo("asdf")
        verify(exactly = 1) { request.get(FssToken::class.java) }
        verify(exactly = 1) { client.target(any<String>()) }

        val second = stsClient.getFssToken()

        assertThat(second).isEqualTo(first)
        verify(exactly = 1) { request.get(FssToken::class.java) }
        verify(exactly = 1) { client.target(any<String>()) }
    }

    @Test
    internal fun utloptTokenSkalFornyes() {
        val tokenMedNegativLevetid = FssToken("asdf", "type", -10L)
        assertThat(stsClient.shouldRenewToken(tokenMedNegativLevetid)).isTrue

        val tokenMedKortLevetid = FssToken("asdf", "type", 1L)
        assertThat(stsClient.shouldRenewToken(tokenMedKortLevetid)).isTrue

        val tokenMedLittLengreLevetid = FssToken("asdf", "type", 12L)
        assertThat(stsClient.shouldRenewToken(tokenMedLittLengreLevetid)).isFalse

        val tokenMedLangLevetid = FssToken("asdf", "type", 3600L)
        assertThat(stsClient.shouldRenewToken(tokenMedLangLevetid)).isFalse
    }
}

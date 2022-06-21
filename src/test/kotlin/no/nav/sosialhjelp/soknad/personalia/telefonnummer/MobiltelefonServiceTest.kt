package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MobiltelefonServiceTest {

    private val krrProxyClient: KrrProxyClient = mockk()
    private val mobiltelefonService = MobiltelefonServiceImpl(krrProxyClient)

    private val ident = "99988877777"
    private val mobiltelefonnummer = "12345678"

    @Test
    internal fun `skal hente mobiltelefonnummer`() {
        every { krrProxyClient.getDigitalKontaktinformasjon(any()) } returns DigitalKontaktinformasjon(ident, true, true, false, mobiltelefonnummer)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isEqualTo(mobiltelefonnummer)
    }

    @Test
    internal fun `skal returnere null hvis DigitalKontaktinfoBolk er null`() {
        every { krrProxyClient.getDigitalKontaktinformasjon(any()) } returns null

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }

    @Test
    internal fun `skal returnere null hvis Mobiltelefonnummer er null`() {
        every { krrProxyClient.getDigitalKontaktinformasjon(any()) } returns DigitalKontaktinformasjon(ident, true, true, false, null)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }
}

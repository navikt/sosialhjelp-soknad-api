package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MobiltelefonServiceTest {

    private val krrService: KrrService = mockk()
    private val mobiltelefonService = MobiltelefonService(krrService)

    private val ident = "99988877777"
    private val mobiltelefonnummer = "12345678"

    private val digitalKontaktinformasjon = DigitalKontaktinformasjon(
        personident = ident,
        aktiv = true,
        kanVarsles = true,
        reservert = false,
        mobiltelefonnummer = mobiltelefonnummer,
    )

    @Test
    internal fun `skal hente mobiltelefonnummer`() {
        every { krrService.getDigitalKontaktinformasjon(any()) } returns digitalKontaktinformasjon

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isEqualTo(mobiltelefonnummer)
    }

    @Test
    internal fun `skal returnere null hvis DigitalKontaktinfoBolk er null`() {
        every { krrService.getDigitalKontaktinformasjon(any()) } returns null

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }

    @Test
    internal fun `skal returnere null hvis Mobiltelefonnummer er null`() {
        every { krrService.getDigitalKontaktinformasjon(any()) } returns digitalKontaktinformasjon.copy(mobiltelefonnummer = null)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }
}

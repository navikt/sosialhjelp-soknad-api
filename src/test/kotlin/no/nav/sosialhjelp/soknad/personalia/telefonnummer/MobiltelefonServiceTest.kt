package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import io.mockk.every
import io.mockk.mockk
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinfo
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinfoBolk
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.DigitalKontaktinformasjon
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.dto.Feil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Collections.singletonMap

internal class MobiltelefonServiceTest {

    private val dkifClient: DkifClient = mockk()
    private val krrProxyClient: KrrProxyClient = mockk()
    private val unleash: Unleash = mockk()
    private val mobiltelefonService = MobiltelefonServiceImpl(dkifClient, krrProxyClient, unleash)

    private val ident = "99988877777"
    private val mobiltelefonnummer = "12345678"

    @BeforeEach
    internal fun setUp() {
        every { unleash.isEnabled(any(), any<Boolean>()) } returns false
    }

    @Test
    internal fun skalHenteMobiltelefonnummer() {
        every { dkifClient.hentDigitalKontaktinfo(any()) } returns DigitalKontaktinfoBolk(singletonMap(ident, DigitalKontaktinfo(mobiltelefonnummer)), null)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isEqualTo(mobiltelefonnummer)
    }

    @Test
    internal fun `skal returnere null hvis DigitalKontaktinfoBolk er null`() {
        every { dkifClient.hentDigitalKontaktinfo(any()) } returns null

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }

    @Test
    internal fun `skal returnere null hvis Kontaktinfo er null`() {
        every { dkifClient.hentDigitalKontaktinfo(any()) } returns DigitalKontaktinfoBolk(null, null)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }

    @Test
    internal fun `skal returnere null hvis Mobiltelefonnummer er null`() {
        every { dkifClient.hentDigitalKontaktinfo(any()) } returns DigitalKontaktinfoBolk(singletonMap(ident, DigitalKontaktinfo(null)), null)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }

    @Test
    internal fun `skal returnere null hvis Feil er satt i response`() {
        every { dkifClient.hentDigitalKontaktinfo(any()) } returns DigitalKontaktinfoBolk(null, singletonMap(ident, Feil("feil feil feil")))

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }

    @Test
    internal fun `krr - skalHenteMobiltelefonnummer`() {
        every { unleash.isEnabled(any(), any<Boolean>()) } returns true
        every { krrProxyClient.getDigitalKontaktinformasjon(any()) } returns DigitalKontaktinformasjon(ident, true, true, false, mobiltelefonnummer)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isEqualTo(mobiltelefonnummer)
    }

    @Test
    internal fun `krr - skal returnere null hvis DigitalKontaktinfoBolk er null`() {
        every { unleash.isEnabled(any(), any<Boolean>()) } returns true
        every { krrProxyClient.getDigitalKontaktinformasjon(any()) } returns null

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }

    @Test
    internal fun `krr - skal returnere null hvis Mobiltelefonnummer er null`() {
        every { unleash.isEnabled(any(), any<Boolean>()) } returns true
        every { krrProxyClient.getDigitalKontaktinformasjon(any()) } returns DigitalKontaktinformasjon(ident, true, true, false, null)

        val response = mobiltelefonService.hent(ident)

        assertThat(response).isNull()
    }
}

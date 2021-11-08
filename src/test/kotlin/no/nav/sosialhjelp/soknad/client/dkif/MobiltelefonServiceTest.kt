package no.nav.sosialhjelp.soknad.client.dkif

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.client.dkif.dto.DigitalKontaktinfo
import no.nav.sosialhjelp.soknad.client.dkif.dto.DigitalKontaktinfoBolk
import no.nav.sosialhjelp.soknad.client.dkif.dto.Feil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Collections.singletonMap

internal class MobiltelefonServiceTest {

    private val dkifClient: DkifClient = mockk()
    private val mobiltelefonService = MobiltelefonServiceImpl(dkifClient)

    private val ident = "99988877777"
    private val mobiltelefonnummer = "12345678"

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
}

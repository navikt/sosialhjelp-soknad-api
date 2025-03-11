package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenClient
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.register.AbstractOkonomiRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseForSkattbarInntektService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class InntektSkatteetatenFetcherTest : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var inntektSkatteetatenFetcher: InntektSkatteetatenFetcher

    @Test
    fun `Hente utbetalinger skal returneres riktig`() {
        createAnswerForSkatteetatenClient()

        inntektSkatteetatenFetcher.fetchInntekt().also { assertThat(it).hasSize(2) }
    }

    @Test
    fun `Clienten returnerer null skal kaste exception`() {
        every { skatteetatenClient.hentSkattbarinntekt(any()) } throws SkatteetatenException("Feil i kall")
        assertThatThrownBy { inntektSkatteetatenFetcher.fetchInntekt() }.isInstanceOf(SkatteetatenException::class.java)
    }

    @SpykBean
    private lateinit var skattbarInntektService: SkattbarInntektService

    @SpykBean
    private lateinit var organisasjonService: OrganisasjonService

    @MockkBean
    private lateinit var skatteetatenClient: SkatteetatenClient

    private fun createAnswerForSkatteetatenClient() {
        every { skattbarInntektService.hentUtbetalinger(any()) } returns defaultResponseForSkattbarInntektService()
        every { organisasjonService.hentOrgNavn(any()) } returns "Navn på arbeidsgiver"
    }
}

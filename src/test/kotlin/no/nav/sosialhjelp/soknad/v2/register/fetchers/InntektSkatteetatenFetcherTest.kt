package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.SamtykkeService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.register.AbstractOkonomiRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseForSkattbarInntektService
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class InntektSkatteetatenFetcherTest : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var inntektSkatteetatenFetcher: InntektSkatteetatenFetcher

    @Autowired
    private lateinit var integrasjonstatusRepository: IntegrasjonstatusRepository

    @Autowired
    private lateinit var samtykkeService: SamtykkeService

    @Test
    fun `Hente inntekt skal lagres i db`() {
        setBekreftelse(true)
        createAnswerForSkatteetatenClient()

        inntektSkatteetatenFetcher.fetch(soknad.id)

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .allMatch { it.type == InntektType.UTBETALING_SKATTEETATEN }
                .allMatch { it.inntektDetaljer.detaljer.size == 2 }
        }
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilInntektSkatteetaten).isFalse()
    }

    @Test
    fun `Ikke bekreftet eller false skal ikke hente opplysninger`() {
        createAnswerForSkatteetatenClient()

        inntektSkatteetatenFetcher.fetch(soknad.id)
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)).isNull()

        setBekreftelse(false)
        inntektSkatteetatenFetcher.fetch(soknad.id)
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()

        setBekreftelse(true)
        inntektSkatteetatenFetcher.fetch(soknad.id)
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).hasSize(1)
    }

    @Test
    fun `Tom liste lagrer ingen Inntekt`() {
        every { skattbarInntektService.hentUtbetalinger(any()) } returns emptyList()

        setBekreftelse(true)
        inntektSkatteetatenFetcher.fetch(soknad.id)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilInntektSkatteetaten).isFalse()
    }

    @Test
    fun `Service returnerer null setter integrasjonstatus feilet til true`() {
        every { skattbarInntektService.hentUtbetalinger(any()) } returns null

        setBekreftelse(true)
        inntektSkatteetatenFetcher.fetch(soknad.id)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilInntektSkatteetaten).isTrue()
    }

    @Test
    fun `Sette samtykke = false skal fjerne inntekt`() {
        createAnswerForSkatteetatenClient()

        setBekreftelse(true)
        inntektSkatteetatenFetcher.fetch(soknad.id)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).hasSize(1)

        samtykkeService.updateSamtykkeSkatteetaten(soknad.id, false)
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).hasSize(0)
    }

    private fun setBekreftelse(samtykke: Boolean) {
        samtykkeService.updateSamtykkeSkatteetaten(soknad.id, samtykkeGitt = samtykke)
    }

    @MockkBean
    private lateinit var skattbarInntektService: SkattbarInntektService

    @MockkBean
    private lateinit var organisasjonService: OrganisasjonService

    private fun createAnswerForSkatteetatenClient() {
        every { skattbarInntektService.hentUtbetalinger(any()) } returns defaultResponseForSkattbarInntektService()
        every { organisasjonService.hentOrgNavn(any()) } returns "Navn på arbeidsgiver"
    }
}

package no.nav.sosialhjelp.soknad.v2.register

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenClient
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektSkatteetatenUseCaseHandler
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType.JOBB
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType.UTBETALING_SKATTEETATEN
import no.nav.sosialhjelp.soknad.v2.register.fetchers.SkatteetatenException
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class InntektSkattUseCaseHandlerTest : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var inntektSkatteetatenUseCaseHandler: InntektSkatteetatenUseCaseHandler

    @Autowired
    private lateinit var integrasjonStatusService: IntegrasjonStatusService

    @Test
    fun `Hente inntekt skal lagres i db`() {
        createAnswerForSkatteetatenClient()

        inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .allMatch { it.type == UTBETALING_SKATTEETATEN }
                .allMatch { it.inntektDetaljer.detaljer.size == 2 }
        }
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isFalse()
    }

    @Test
    fun `Tom liste lagrer ingen Inntekt`() {
        every { skattbarInntektService.hentUtbetalinger(any()) } returns emptyList()

        inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isFalse()
    }

    @Test
    fun `Service returnerer null setter integrasjonstatus feilet til true, og oppretter InntektType JOBB`() {
        every { skatteetatenClient.hentSkattbarinntekt(any()) } throws SkatteetatenException("Feil ved henting")

        inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.toList())
            .hasSize(1)
            .allMatch { it.type == JOBB }
        assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, JOBB)).isNotNull()
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isTrue()
    }

    @Test
    fun `Sette samtykke = false skal fjerne inntekt og opprette JOBB dokumentasjon`() {
        createAnswerForSkatteetatenClient()

        inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.toList())
            .hasSize(1)
            .allMatch { it.type == UTBETALING_SKATTEETATEN }

        inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, false)
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.toList())
            .hasSize(1)
            .allMatch { it.type == JOBB }

        assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, JOBB)).isNotNull()
    }

    private fun createAnswerForSkatteetatenClient() {
        every { skattbarInntektService.hentUtbetalinger(any()) } returns defaultResponseForSkattbarInntektService()
        every { organisasjonService.hentOrgNavn(any()) } returns "Navn på arbeidsgiver"
    }

    @SpykBean
    private lateinit var skattbarInntektService: SkattbarInntektService

    @SpykBean
    private lateinit var organisasjonService: OrganisasjonService

    @MockkBean
    private lateinit var skatteetatenClient: SkatteetatenClient
}

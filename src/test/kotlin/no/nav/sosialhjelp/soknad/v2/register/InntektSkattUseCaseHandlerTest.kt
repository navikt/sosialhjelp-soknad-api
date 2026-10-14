package no.nav.sosialhjelp.soknad.v2.register

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektResponse
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenClient
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType.JOBB
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType.UTBETALING_SKATTEETATEN
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektSkatteetatenUseCaseHandler
import no.nav.sosialhjelp.soknad.v2.register.fetchers.InntektSkatteetatenFetcher
import no.nav.sosialhjelp.soknad.v2.register.fetchers.SkatteetatenException
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class InntektSkattUseCaseHandlerTest(@Autowired private val okonomiService: OkonomiService) : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var inntektSkatteetatenUseCaseHandler: InntektSkatteetatenUseCaseHandler

    @Autowired
    private lateinit var integrasjonStatusService: IntegrasjonStatusService

    @Autowired
    private lateinit var inntektSkatteetatenFetcher: InntektSkatteetatenFetcher

    @Test
    fun `Hente inntekt skal lagres i db`() {
        createAnswerForSkatteetatenClient()

//        inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)

        runBlocking {inntektSkatteetatenFetcher.fetchAndSave(soknad.id)}


        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .allMatch { it.type == UTBETALING_SKATTEETATEN }
                .allMatch { it.inntektDetaljer.detaljer.size == 2 }
        }
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isFalse()
    }

    @Test
    fun `Tom liste lagrer ingen Inntekt`() {
        coEvery { skattbarInntektService.hentInntekt() } returns emptyList()

        //inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)
        runBlocking {inntektSkatteetatenFetcher.fetchAndSave(soknad.id)}

        //assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(okonomiService.getInntekter(soknad.id)).isEmpty()
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isFalse()
    }

    @Test
    fun `Service returnerer null setter integrasjonstatus feilet til true`() {
        coEvery { skatteetatenClient.hentSkattbarinntekt() } returns SkattbarInntektResponse.Error("Feil", SkatteetatenException("error"))

//        inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)

        runBlocking {inntektSkatteetatenFetcher.fetchAndSave(soknad.id)}


//        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.toList())
//            .hasSize(1)
//            .allMatch { it.type == JOBB }
//        assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, JOBB)).isNotNull()
        assertThat(okonomiService.getInntekter(soknad.id)).isEmpty()
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isTrue()
    }

//    @Test
//    fun `Sette samtykke = false skal fjerne inntekt og opprette JOBB dokumentasjon`() {
//        createAnswerForSkatteetatenClient()
//
//        //inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, true)
//        runBlocking {inntektSkatteetatenFetcher.fetchAndSave(soknad.id)}
//
//
//        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.toList())
//            .hasSize(1)
//            .allMatch { it.type == UTBETALING_SKATTEETATEN }
//
//        //inntektSkatteetatenUseCaseHandler.updateSamtykke(soknad.id, false)
//        runBlocking {inntektSkatteetatenFetcher.fetchAndSave(soknad.id)}
//
//        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.toList())
//            .hasSize(1)
//            .allMatch { it.type == JOBB }
//
//        assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, JOBB)).isNotNull()
//    }

    private fun createAnswerForSkatteetatenClient() {
        coEvery { skattbarInntektService.hentInntekt() } returns defaultResponseForSkattbarInntektService()
        every { organisasjonService.hentOrgNavn(any()) } returns "Navn på arbeidsgiver"
    }

    @MockkSpyBean
    private lateinit var skattbarInntektService: SkattbarInntektService

    @MockkSpyBean
    private lateinit var organisasjonService: OrganisasjonService

    @MockkBean
    private lateinit var skatteetatenClient: SkatteetatenClient
}

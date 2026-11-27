package no.nav.sosialhjelp.soknad.v2.register

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.runBlocking
import no.nav.sosialhjelp.soknad.app.subjecthandler.UserContextElement
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektResponse
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenClient
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType.UTBETALING_SKATTEETATEN
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.register.fetchers.InntektSkatteetatenFetcher
import no.nav.sosialhjelp.soknad.v2.register.fetchers.SkatteetatenException
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class InntektSkattUseCaseHandlerTest(
    @Autowired private val okonomiService: OkonomiService,
) : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var integrasjonStatusService: IntegrasjonStatusService

    @Autowired
    private lateinit var inntektSkatteetatenFetcher: InntektSkatteetatenFetcher

    @Test
    fun `Hente inntekt skal lagres i db`() {
        createAnswerForSkatteetatenClient()

        runBlocking(UserContextElement("dummy-token", soknad.eierPersonId)) {
            okonomiService.addElementToOkonomi(soknad.id, InntektType.JOBB)
            assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, InntektType.JOBB)).isNotNull()

            inntektSkatteetatenFetcher.fetchAndSave(soknad.id)
        }

        val inntekter = okonomiService.getInntekter(soknad.id)
        assertThat(inntekter.map { it.type }).containsExactlyInAnyOrder(InntektType.JOBB, UTBETALING_SKATTEETATEN)
        assertThat(inntekter.single { it.type == UTBETALING_SKATTEETATEN }.inntektDetaljer.detaljer).hasSize(2)
        assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, InntektType.JOBB)).isNotNull()
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isFalse()
    }

    @Test
    fun `Tom liste lagrer ingen Inntekt`() {
        coEvery { skattbarInntektService.hentInntekt() } returns emptyList()

        runBlocking(UserContextElement("dummy-token", soknad.eierPersonId)) {
            okonomiService.addElementToOkonomi(soknad.id, InntektType.JOBB)
            assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, InntektType.JOBB)).isNotNull()

            inntektSkatteetatenFetcher.fetchAndSave(soknad.id)
        }

        assertThat(okonomiService.getInntekter(soknad.id).map { it.type }).containsExactly(InntektType.JOBB)
        assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, InntektType.JOBB)).isNotNull()
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isFalse()
    }

    @Test
    fun `Service returnerer null setter integrasjonstatus feilet til true`() {
        coEvery { skatteetatenClient.hentSkattbarinntekt() } returns SkattbarInntektResponse.Error("Feil", SkatteetatenException("error"))

        runBlocking { inntektSkatteetatenFetcher.fetchAndSave(soknad.id) }

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter.toList())
            .hasSize(1)
            .allMatch { it.type == InntektType.JOBB }
        assertThat(dokumentasjonRepository.findBySoknadIdAndType(soknad.id, InntektType.JOBB)).isNotNull()
        assertThat(integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknad.id)).isTrue()
    }

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

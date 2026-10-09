package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenResponse
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.register.AbstractOkonomiRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseForHusbankenClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import kotlinx.coroutines.runBlocking

class HusbankenServiceTest : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var service: HusbankenService

    @Test
    fun `Hente bostotte-saker skal lagres i db`() {
        createAnswerForHusbankenClient()

        runBlocking { service.getBostotte() }
            .also { (saker, inntekt) ->
                assertThat(saker)
                    .hasSize(2)
                    .anyMatch { it.status == BostotteStatus.VEDTATT }
                    .anyMatch { it.status == BostotteStatus.UNDER_BEHANDLING }
                    .allMatch { it is BostotteSak }

                assertThat(inntekt!!.type).isEqualTo(InntektType.UTBETALING_HUSBANKEN)
                assertThat(inntekt.inntektDetaljer.detaljer.toList())
                    .hasSize(2)
                    .allMatch { it is Utbetaling }
                    .allMatch { (it as Utbetaling).netto != null }
                    .allMatch { (it as Utbetaling).mottaker != null }
            }
    }

    @Test
    fun `Tomme lister lagrer ikke data`() {
        coEvery { husbankenClient.getBostotte(any(), any()) } returns HusbankenResponse.Success(BostotteDto(emptyList(), emptyList()))

        runBlocking { service.getBostotte() }
        okonomiRepository.save(Okonomi(soknad.id))

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.bostotteSaker).isEmpty()
    }

    @MockkBean
    private lateinit var husbankenClient: HusbankenClient

    private fun createAnswerForHusbankenClient() {
        coEvery { husbankenClient.getBostotte(any(), any()) } returns defaultResponseForHusbankenClient()
    }
}

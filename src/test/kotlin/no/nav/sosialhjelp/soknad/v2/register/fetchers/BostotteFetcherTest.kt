package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenResponse
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import no.nav.sosialhjelp.soknad.v2.register.AbstractRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseForHusbankenClient
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClientResponseException

class BostotteFetcherTest(@Autowired private val okonomiRepository: OkonomiRepository) : AbstractRegisterDataTest() {

    @Autowired
    private lateinit var bostotteFetcher: BostotteFetcher

    @Autowired
    private lateinit var integrasjonstatusRepository: IntegrasjonstatusRepository

    @Autowired
    private lateinit var husbankenService: HusbankenService

    @MockkBean
    private lateinit var husbankenClient: HusbankenClient


    @Test
    suspend fun `lagrer saker og utbetalinger og setter status til vellykket`() {
        //coEvery { husbankenService.getBostotte() } returns Pair(defaultResponseForHusbankenClient().bostotte.saker[0, defaultResponseForHusbankenClient().bostotte)
        coEvery { husbankenClient.getBostotte(any(), any()) } returns defaultResponseForHusbankenClient()

        bostotteFetcher.fetchAndSave(soknad.id)

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.bostotteSaker).hasSize(2)
                .anyMatch { it.status == BostotteStatus.VEDTATT }

            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .anyMatch { it.type == InntektType.UTBETALING_HUSBANKEN }
        }
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilStotteHusbanken).isFalse()

    }

    @Disabled
    @Test
    fun `oppretter ikke forventet dokumentasjon ved vellykket tomt svar`() {
        TODO("Sett opp tomt Husbanken-svar, kall fetchAndSave og kontroller dokumentasjon og status")
    }

    @Disabled
    @Test
    suspend fun `oppretter forventet dokumentasjon og setter feilstatus ved hentefeil`() {
        coEvery { husbankenClient.getBostotte(any(), any()) } returns
                HusbankenResponse.Error(
                    WebClientResponseException
                        .create(500, "Feil", HttpHeaders(), ByteArray(0), null),
                )


        bostotteFetcher.fetchAndSave(soknad.id)

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->
            assertThat(okonomi.bostotteSaker).hasSize(0)

            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .anyMatch { it.type == InntektType.UTBETALING_HUSBANKEN }
        }
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilStotteHusbanken).isTrue()
    }

}

package no.nav.sosialhjelp.soknad.v2.register.handlers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import no.nav.sosialhjelp.soknad.v2.okonomi.SamtykkeService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.register.AbstractOkonomiRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseForHusbankenClient
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class BostotteHusbankenFetcherTest : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var fetcher: BostotteHusbankenFetcher

    @Autowired
    private lateinit var integrasjonstatusRepository: IntegrasjonstatusRepository

    @Autowired
    private lateinit var samtykkeService: SamtykkeService

    @Test
    fun `Hente bostotte-saker skal lagres i db`() {
        createAnswerForHusbankenClient()

        setSamtykke(true)
        fetcher.fetchAndSave(soknad.id, "token")

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->

            assertThat(okonomi.bostotteSaker).hasSize(2)
            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .allMatch { it.type == InntektType.UTBETALING_HUSBANKEN }
                .allMatch { it.inntektDetaljer.detaljer.size == 2 }
        }
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilStotteHusbanken).isFalse()
    }

    @Test
    fun `Ikke gitt samtykke eller samtykke = false skal ikke lagre data`() {
        createAnswerForHusbankenClient()

        fetcher.fetchAndSave(soknad.id, "token")
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.bostotteSaker).isEmpty()

        setSamtykke(false)
        fetcher.fetchAndSave(soknad.id, "token")
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.bostotteSaker).isEmpty()

        setSamtykke(true)
        fetcher.fetchAndSave(soknad.id, "token")
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).hasSize(1)
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.bostotteSaker).hasSize(2)
    }

    @Test
    fun `Tomme lister lagrer ikke data`() {
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns BostotteDto(emptyList(), emptyList())

        setSamtykke(true)
        fetcher.fetchAndSave(soknad.id, "token")

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.inntekter).isEmpty()
        assertThat(okonomiRepository.findByIdOrNull(soknad.id)!!.bostotteSaker).isEmpty()
    }

    @Test
    fun `Client returnerer null setter integrasjonstatus feilet til true`() {
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns null

        setSamtykke(true)
        fetcher.fetchAndSave(soknad.id, "token")

        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilStotteHusbanken).isTrue()
    }

    private fun setSamtykke(gitt: Boolean) {
        samtykkeService.updateSamtykkeBostotte(soknad.id, gitt)
    }

    @MockkBean
    private lateinit var husbankenClient: HusbankenClient

    private fun createAnswerForHusbankenClient() {
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns defaultResponseForHusbankenClient()
    }
}

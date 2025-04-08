package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerClient
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.dto.UtbetalDataDto
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.v2.register.AbstractOkonomiRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromNavUtbetalingerClient
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonstatusRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class UtbetalingerFraNavFetcherTest : AbstractOkonomiRegisterDataTest() {
    @Autowired
    private lateinit var utbetalingerFraNavFetcher: UtbetalingerFraNavFetcher

    @Autowired
    private lateinit var integrasjonstatusRepository: IntegrasjonstatusRepository

    @Test
    fun `Utbetalinger fra NAV skal lagres i db`() {
        createAnswerForNavUtbetalingerClient()
        utbetalingerFraNavFetcher.fetchAndSave(soknad.id)

        okonomiRepository.findByIdOrNull(soknad.id)!!.also { okonomi ->

            assertThat(okonomi.inntekter.toList()).hasSize(1)
                .anyMatch { it.type == InntektType.UTBETALING_NAVYTELSE }

            val inntektDetaljer = okonomi.inntekter.first().inntektDetaljer
            assertThat(inntektDetaljer.detaljer).hasSize(4)
                .allMatch { it is UtbetalingMedKomponent }
        }
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilUtbetalingerNav).isFalse()
    }

    @Test
    fun `Tom liste lagrer ingen Inntekt`() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns
            UtbetalDataDto(utbetalinger = emptyList(), feilet = false)

        utbetalingerFraNavFetcher.fetchAndSave(soknad.id)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)).isNull()
        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilUtbetalingerNav).isFalse()
    }

    @Test
    fun `Returnerer null setter integrasjon-status feilet = true`() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns null

        utbetalingerFraNavFetcher.fetchAndSave(soknad.id)

        assertThat(okonomiRepository.findByIdOrNull(soknad.id)).isNull()

        assertThat(integrasjonstatusRepository.findByIdOrNull(soknad.id)!!.feilUtbetalingerNav).isTrue()
    }

    @MockkBean
    private lateinit var navUtbetalingerClient: NavUtbetalingerClient

    private fun createAnswerForNavUtbetalingerClient() {
        every { navUtbetalingerClient.getUtbetalingerSiste40Dager(any()) } returns defaultResponseFromNavUtbetalingerClient()
    }
}

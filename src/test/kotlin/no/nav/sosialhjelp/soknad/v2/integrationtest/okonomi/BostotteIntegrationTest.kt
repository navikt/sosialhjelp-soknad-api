package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.SakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.UtbetalingDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.VedtakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteMottaker
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteRolle
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteStatus.UNDER_BEHANDLING
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteStatus.VEDTATT
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteDto
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteInput
import no.nav.sosialhjelp.soknad.v2.bostotte.SamtykkeInput
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.Mottaker
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljer
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto as HusbankenDto

class BostotteIntegrationTest : AbstractOkonomiIntegrationTest() {
    @Autowired
    private lateinit var okonomiService: OkonomiService

    @MockkBean
    private lateinit var husbankenClient: HusbankenClient

    @Test
    fun `Get skal returnere lagrede data`() {
        opprettBostotteData()

        doGet(
            uri = bostottUrl(soknad.id),
            responseBodyClass = BostotteDto::class.java,
        ).also { dto ->
            assertThat(dto.hasBostotte).isTrue()
            assertThat(dto.hasSamtykke).isTrue()
            assertThat(dto.utbetalinger).hasSize(1)
            assertThat(dto.saker).hasSize(1)
        }
    }

    @Test
    fun `Oppdatere bostotte til false skal slette innhentet data`() {
        opprettBostotteData()

        doPut(
            uri = bostottUrl(soknad.id),
            requestBody = BostotteInput(hasBostotte = false),
            responseBodyClass = BostotteDto::class.java,
            soknadId = soknad.id,
        ).also { dto ->
            assertThat(dto.hasBostotte).isFalse()
            assertThat(dto.hasSamtykke).isNull()
            assertThat(dto.utbetalinger).isEmpty()
            assertThat(dto.saker).isEmpty()
        }
    }

    @Test
    fun `Sette samtykke false skal slette innhentet date`() {
        opprettBostotteData()

        doPost(
            uri = bostottUrl(soknad.id),
            requestBody = SamtykkeInput(hasSamtykke = false),
            responseBodyClass = BostotteDto::class.java,
            soknadId = soknad.id,
        ).also { dto ->
            assertThat(dto.hasBostotte).isTrue()
            assertThat(dto.hasSamtykke).isFalse()
            assertThat(dto.utbetalinger).isEmpty()
            assertThat(dto.saker).isEmpty()
        }
    }

    @Test
    fun `Sette samtykke true uten bostotte skal returnere bad request`() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, false)

        doPostFullResponse(
            uri = bostottUrl(soknad.id),
            requestBody = SamtykkeInput(hasSamtykke = true),
            soknadId = soknad.id,
        )
            .expectStatus().isBadRequest
    }

    // TODO Skal vi alltid hente inn på nytt i dette tilfellet - eller skal vi ha annen logikk basert på dato?
    @Test
    fun `Oppdatere samtykke som var true til true skal ikke trigge ny innhenting`() {
        opprettBostotteData()

        doPost(
            uri = bostottUrl(soknad.id),
            requestBody = SamtykkeInput(hasSamtykke = true),
            responseBodyClass = BostotteDto::class.java,
            soknadId = soknad.id,
        )

        verify(exactly = 0) { husbankenClient.hentBostotte(any(), any(), any()) }
    }

    @Test
    fun `Oppdatere samtykke som var false til true skal trigge ny innhenting fra register`() {
        setupHusbankenAnswer()

        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, true)

        doPost(
            uri = bostottUrl(soknad.id),
            requestBody = SamtykkeInput(hasSamtykke = true),
            responseBodyClass = BostotteDto::class.java,
            soknadId = soknad.id,
        ).also { dto ->
            assertThat(dto.hasBostotte).isTrue()
            assertThat(dto.hasSamtykke).isTrue()
            assertThat(dto.saker).hasSize(createSaker().size)
            assertThat(dto.utbetalinger).hasSize(createUtbetalinger().size)
        }

        verify(exactly = 1) { husbankenClient.hentBostotte(any(), any(), any()) }
    }

    private fun opprettBostotteData() {
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, true)
        okonomiService.addElementToOkonomi(
            soknadId = soknad.id,
            element =
                Inntekt(
                    type = InntektType.UTBETALING_HUSBANKEN,
                    inntektDetaljer =
                        OkonomiDetaljer(
                            detaljer =
                                listOf(
                                    Utbetaling(
                                        netto = 2500.0,
                                        mottaker = Mottaker.HUSSTAND,
                                        utbetalingsdato = LocalDate.now().minusDays(1),
                                    ),
                                ),
                        ),
                ),
        )
        okonomiService.addBostotteSak(
            soknadId = soknad.id,
            sak =
                BostotteSak(
                    dato = LocalDate.now().minusDays(1),
                    status = BostotteStatus.UNDER_BEHANDLING,
                    beskrivelse = "Beskrivelse av Bostottesak",
                    vedtaksstatus = null,
                ),
        )
    }

    private fun setupHusbankenAnswer() {
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns createBostotteDto()
    }

    private fun createBostotteDto() =
        HusbankenDto(
            saker = createSaker(),
            utbetalinger = createUtbetalinger(),
        )

    private fun createSaker(): List<SakDto> =
        listOf(
            SakDto(
                today.monthValue,
                today.year,
                UNDER_BEHANDLING,
                VedtakDto("1234", "besk", Vedtaksstatus.INNVILGET.name),
                BostotteRolle.HOVEDPERSON,
            ),
            SakDto(
                today.monthValue,
                today.year,
                VEDTATT,
                VedtakDto("5534", "beskr", Vedtaksstatus.AVVIST.name),
                BostotteRolle.HOVEDPERSON,
            ),
        )

    private fun createUtbetalinger(): List<UtbetalingDto> =
        listOf(
            UtbetalingDto(
                LocalDate.now().minusDays(5),
                BigDecimal.valueOf(4300.0),
                BostotteMottaker.HUSSTAND,
                BostotteRolle.HOVEDPERSON,
            ),
            UtbetalingDto(
                LocalDate.now().minusDays(20),
                BigDecimal.valueOf(5300.0),
                BostotteMottaker.KOMMUNE,
                BostotteRolle.HOVEDPERSON,
            ),
        )

    companion object {
        private fun bostottUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/bostotte"

        private val today = LocalDate.now()
    }
}

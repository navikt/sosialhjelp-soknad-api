package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import com.ninjasquad.springmockk.MockkBean
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
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
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

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

        val bostotteDto =
            doPost(
                uri = bostottUrl(soknad.id),
                requestBody = SamtykkeInput(hasSamtykke = false),
                responseBodyClass = BostotteDto::class.java,
                soknadId = soknad.id,
            )

        val a = 4
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

    companion object {
        private fun bostottUrl(soknadId: UUID) = "/soknad/$soknadId/inntekt/bostotte"
    }
}

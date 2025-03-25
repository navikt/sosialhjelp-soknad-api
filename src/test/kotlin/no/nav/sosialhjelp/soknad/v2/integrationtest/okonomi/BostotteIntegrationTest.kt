package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenException
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.SakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.UtbetalingDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.VedtakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteMottaker
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteRolle
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteStatus.UNDER_BEHANDLING
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteStatus.VEDTATT
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteDto
import no.nav.sosialhjelp.soknad.v2.bostotte.BostotteInput
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import no.nav.sosialhjelp.soknad.v2.eier.EierRepository
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Belop
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

    @Autowired
    private lateinit var jsonInternalSoknadGenerator: JsonInternalSoknadGenerator

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

        postBostotte(false)
            .also { dto ->
                assertThat(dto.hasBostotte).isFalse()
                assertThat(dto.hasSamtykke).isNull()
                assertThat(dto.utbetalinger).isEmpty()
                assertThat(dto.saker).isEmpty()
            }
    }

    @Test
    fun `Sette samtykke false skal slette innhentet data`() {
        opprettBostotteData()

        postBostotte(hasBostotte = true, hasSamtykke = false)
            .also { dto ->
                assertThat(dto.hasBostotte).isTrue()
                assertThat(dto.hasSamtykke).isFalse()
                assertThat(dto.utbetalinger).isEmpty()
                assertThat(dto.saker).isEmpty()
            }
    }

    @Test
    fun `Oppdatere samtykke som var true til true skal ikke trigge ny innhenting`() {
        integrasjonStatusService.setStotteHusbankenStatus(soknad.id, false)

        opprettBostotteData()

        postBostotte(true)

        verify(exactly = 0) { husbankenClient.hentBostotte(any(), any()) }
    }

    @Test
    fun `Oppdatere samtykke som var false til true skal trigge ny innhenting fra register`() {
        setupHusbankenAnswer()

        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, true)

        postBostotte(hasBostotte = true, hasSamtykke = true)
            .also { dto ->
                assertThat(dto.hasBostotte).isTrue()
                assertThat(dto.hasSamtykke).isTrue()
                assertThat(dto.saker).hasSize(createSaker().size)
                assertThat(dto.utbetalinger).hasSize(createUtbetalinger().size)
            }

        verify(exactly = 1) { husbankenClient.hentBostotte(any(), any()) }
    }

    @Test
    fun `Skal hverken genereres inntekt eller dokumentasjon ved bostotte false`() {
        postBostotte(false)

        okonomiService.getInntekter(soknad.id).also { assertThat(it).isEmpty() }
        okonomiService.getBekreftelser(soknad.id)
            .also { bekreftelser ->
                assertThat(bekreftelser.toList()).hasSize(1)
                    .anyMatch { it.type == BekreftelseType.BOSTOTTE && !it.verdi }
            }
    }

    @Test
    fun `Skal genereres inntekt og dokumentasjon ved bostotte true men ingen samtykke`() {
        postBostotte(true)
        assertInntektOgDokumentasjon(hasSamtykke = null)
    }

    @Test
    fun `Skal genereres inntekt og dokumentasjon ved bostotte true men samtykke false`() {
        postBostotte(hasBostotte = true, hasSamtykke = false)
        assertInntektOgDokumentasjon(hasSamtykke = false)
    }

    @Test
    fun `Skal finnes inntekt og dokumentasjon ved bostotte og samtykke true, men innhenting feilet`() {
        every { husbankenClient.hentBostotte(any(), any()) } throws HusbankenException("Feilet")

        postBostotte(hasBostotte = true, true)
        assertInntektOgDokumentasjon(hasSamtykke = true)
        assertThat(integrasjonStatusService.hasFetchHusbankenFailed(soknad.id)).isTrue()
    }

    @Test
    fun `Sette bostotte til false skal fjerne inntekter og samtykke`() {
        setupHusbankenAnswer()

        postBostotte(hasBostotte = true, hasSamtykke = true)

        assertInntektOgDokumentasjon(hasSamtykke = true)
        okonomiService.getBostotteSaker(soknad.id).also { assertThat(it).hasSize(2) }

        postBostotte(false)
        okonomiService.getBekreftelser(soknad.id)
            .also { bekreftelser ->
                assertThat(bekreftelser.toList()).hasSize(1)
                    .anyMatch { it.type == BekreftelseType.BOSTOTTE && !it.verdi }
            }
        okonomiService.getInntekter(soknad.id).also { assertThat(it).isEmpty() }
        okonomiService.getBostotteSaker(soknad.id).also { assertThat(it).isEmpty() }
    }

    @Test
    fun `Informasjon om utbetaling fra Husbanken skal vises`() {
        val belop = 12345.0

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
                                        netto = belop,
                                        utbetalingsdato = LocalDate.now().minusDays(10),
                                        mottaker = Mottaker.HUSSTAND,
                                    ),
                                ),
                        ),
                ),
        )

        val dto =
            doGet(
                uri = bostottUrl(soknad.id),
                responseBodyClass = BostotteDto::class.java,
            )
        assertThat(dto.utbetalinger).hasSize(1)
        dto.utbetalinger.first()
            .let {
                assertThat(it.netto).isEqualTo(belop)
                assertThat(it.utbetalingsdato).isNotNull()
                assertThat(it.mottaker).isNotNull
            }
    }

    @Test
    fun `Informasjon fra bruker om utbetaling fra Husbanken skal ikke komme i DTO`() {
        okonomiService.addElementToOkonomi(
            soknadId = soknad.id,
            element =
                Inntekt(
                    type = InntektType.UTBETALING_HUSBANKEN,
                    inntektDetaljer = OkonomiDetaljer(detaljer = listOf(Belop(belop = 12345.0))),
                ),
        )

        val dto =
            doGet(
                uri = bostottUrl(soknad.id),
                responseBodyClass = BostotteDto::class.java,
            )
        assertThat(dto.utbetalinger).hasSize(1)
        dto.utbetalinger.first()
            .let {
                assertThat(it.netto).isNull()
                assertThat(it.utbetalingsdato).isNull()
                assertThat(it.mottaker).isNull()
            }
    }

    @Test
    fun `Mapping av informasjon fra Husbanken skal gi kilde system og belop i feltet netto`() {
        eierRepository.createEier(soknad.id)
        kontaktRepository.createAdresser(soknad.id)

        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = true)

        okonomiService.addElementToOkonomi(
            soknadId = soknad.id,
            element =
                Inntekt(
                    type = InntektType.UTBETALING_HUSBANKEN,
                    inntektDetaljer = OkonomiDetaljer(detaljer = listOf(Utbetaling(netto = 12345.0))),
                ),
        )

        val json = jsonInternalSoknadGenerator.createJsonInternalSoknad(soknad.id)

        with(json.soknad.data.okonomi.opplysninger.utbetaling) {
            assertThat(this).hasSize(1)
            this.first()
                .let {
                    assertThat(it.kilde).isEqualTo(JsonKilde.SYSTEM)
                    assertThat(it.belop == null).isTrue()
                    assertThat(it.netto).isNotNull()
                }
        }
    }

    @Test
    fun `Mapping av informasjon om utbetaling fra Husbanken skal gi kilde bruker og belop i feltet belop`() {
        eierRepository.createEier(soknad.id)
        kontaktRepository.createAdresser(soknad.id)

        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE, verdi = true)
        okonomiService.updateBekreftelse(soknad.id, BekreftelseType.BOSTOTTE_SAMTYKKE, verdi = false)

        okonomiService.addElementToOkonomi(
            soknadId = soknad.id,
            element =
                Inntekt(
                    type = InntektType.UTBETALING_HUSBANKEN,
                    inntektDetaljer = OkonomiDetaljer(detaljer = listOf(Belop(belop = 12345.0))),
                ),
        )

        val json = jsonInternalSoknadGenerator.createJsonInternalSoknad(soknad.id)

        with(json.soknad.data.okonomi.opplysninger.utbetaling) {
            assertThat(this).hasSize(1)
            this.first()
                .let {
                    assertThat(it.kilde).isEqualTo(JsonKilde.BRUKER)
                    assertThat(it.netto == null).isTrue()
                    assertThat(it.belop).isNotNull()
                }
        }
    }

    @Test
    fun `Skal ha forventet dokumentasjon hvis clienten kaster exception`() {
        every { husbankenClient.hentBostotte(any(), any()) } throws HusbankenException("Feilet")

        postBostotte(hasBostotte = true, true)

        dokRepository.findBySoknadIdAndType(soknad.id, InntektType.UTBETALING_HUSBANKEN)
            .also { assertThat(it).isNotNull }
        okonomiService.getInntekter(soknad.id)
            .find { it.type == InntektType.UTBETALING_HUSBANKEN }
            .also { assertThat(it).isNotNull }
    }

    private fun postBostotte(
        hasBostotte: Boolean?,
        hasSamtykke: Boolean? = null,
    ): BostotteDto {
        return doPost(
            uri = bostottUrl(soknad.id),
            requestBody = BostotteInput(hasBostotte, hasSamtykke),
            responseBodyClass = BostotteDto::class.java,
            soknadId = soknad.id,
        )
    }

    private fun assertInntektOgDokumentasjon(hasSamtykke: Boolean?) {
        okonomiService.getInntekter(soknad.id)
            .also { inntekter ->
                assertThat(inntekter.toList())
                    .hasSize(1)
                    .allMatch { it.type == InntektType.UTBETALING_HUSBANKEN }
            }
        okonomiService.getBekreftelser(soknad.id)
            .also { bekreftelser ->
                assertThat(bekreftelser.toList())
                    .anyMatch { it.type == BekreftelseType.BOSTOTTE }

                hasSamtykke?.also {
                    assertThat(bekreftelser.toList())
                        .hasSize(2)
                        .anyMatch {
                            it.type == BekreftelseType.BOSTOTTE_SAMTYKKE && it.verdi == hasSamtykke
                        }
                }
            }
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
        every { husbankenClient.hentBostotte(any(), any()) } returns createBostotteDto()
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

private fun EierRepository.createEier(id: UUID) {
    Eier(
        soknadId = id,
        statsborgerskap = "NOR",
        nordiskBorger = true,
        navn = Navn(fornavn = "Fornavn", mellomnavn = "", etternavn = "Etternavn"),
    )
        .also { save(it) }
}

private fun KontaktRepository.createAdresser(id: UUID) {
    Kontakt(
        soknadId = id,
        adresser =
            Adresser(
                adressevalg = AdresseValg.FOLKEREGISTRERT,
                folkeregistrert = VegAdresse(),
            ),
        mottaker =
            NavEnhet(
                enhetsnavn = "NavEnhet",
                enhetsnummer = "123456",
                kommunenummer = "0302",
                orgnummer = "12345678",
                kommunenavn = "Ett eller annet sted",
            ),
    )
        .also { save(it) }
}

package no.nav.sosialhjelp.soknad.inntekt.husbanken

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak.Vedtaksstatus
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.BostotteDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.SakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.UtbetalingDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.dto.VedtakDto
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteMottaker
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteRolle
import no.nav.sosialhjelp.soknad.inntekt.husbanken.enums.BostotteStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import java.util.stream.Collectors

internal class BostotteSystemdataTest {

    private val husbankenClient: HusbankenClient = mockk()
    private val textService: TextService = mockk()
    private val bostotteSystemdata = BostotteSystemdata(husbankenClient, textService)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
    }

    @Test
    fun updateSystemdata_soknadBlirOppdatertMedUtbetalingFraHusbanken() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val mottaker = BostotteMottaker.HUSSTAND
        val netto = BigDecimal.valueOf(10000.5)
        val utbetalingsDato = LocalDate.now()
        val bostotteDto = BostotteDto(
            emptyList(),
            listOf(UtbetalingDto(utbetalingsDato, netto, mottaker, BostotteRolle.HOVEDPERSON))
        )

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetalinger).hasSize(1)
        val utbetaling = utbetalinger[0]
        assertThatUtbetalingErKorrekt(mottaker, netto, utbetaling, utbetalingsDato)
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isFalse
    }

    @Test
    fun updateSystemdata_soknadBlirOppdatertMedToUtbetalingerFraHusbanken() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val mottaker = BostotteMottaker.HUSSTAND
        val netto1 = BigDecimal.valueOf(10000)
        val netto2 = BigDecimal.valueOf(20000)
        val utbetalingsDato = LocalDate.now()
        val bostotteDto = BostotteDto(
            emptyList(),
            listOf(
                UtbetalingDto(utbetalingsDato.minusDays(32), netto1, mottaker, BostotteRolle.HOVEDPERSON),
                UtbetalingDto(utbetalingsDato, netto2, mottaker, BostotteRolle.HOVEDPERSON)
            )
        )

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetalinger).hasSize(1)
        val utbetaling1 = utbetalinger[0]
        assertThatUtbetalingErKorrekt(mottaker, netto2, utbetaling1, utbetalingsDato)
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isFalse
    }

    @Test
    fun updateSystemdata_soknadBlirOppdatertMedSakFraHusbanken() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val sakDto = lagSak(
            LocalDate.now().withDayOfMonth(1),
            BostotteStatus.UNDER_BEHANDLING,
            BostotteRolle.HOVEDPERSON,
            null,
            null,
            null
        )
        val bostotteDto = BostotteDto(listOf(sakDto), emptyList())

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker).hasSize(1)
        val sak = saker[0]
        assertThat(sak.kilde).isEqualTo(JsonKildeSystem.SYSTEM)
        assertThat(sak.type).isEqualTo(SoknadJsonTyper.UTBETALING_HUSBANKEN)
        assertThat(sak.dato).isEqualTo(LocalDate.of(sakDto.ar, sakDto.mnd, 1).toString())
        assertThat(sak.status).isEqualToIgnoringCase(sakDto.status.toString())
        assertThat(sak.beskrivelse).isNull()
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isFalse
    }

    @Test
    fun updateSystemdata_soknadBlirOppdatertMedToSakerFraHusbanken() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val sakDto1 = lagSak(
            LocalDate.now().withDayOfMonth(1),
            BostotteStatus.UNDER_BEHANDLING,
            BostotteRolle.HOVEDPERSON,
            null,
            null,
            null
        )
        val sakDto2 = lagSak(
            LocalDate.now().withDayOfMonth(1),
            BostotteStatus.VEDTATT,
            BostotteRolle.HOVEDPERSON,
            "V02",
            "Avslag - For høy inntekt",
            Vedtaksstatus.AVSLAG
        )
        val bostotteDto = BostotteDto(listOf(sakDto1, sakDto2), emptyList())

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker).hasSize(2)
        val sak1 = saker[0]
        val sak2 = saker[1]
        assertThat(sak1.dato).isEqualTo(LocalDate.of(sakDto1.ar, sakDto1.mnd, 1).toString())
        assertThat(sak1.status).isEqualToIgnoringCase(sakDto1.status.toString())
        assertThat(sak1.beskrivelse).isNull()
        assertThat(sak2.dato).isEqualTo(LocalDate.of(sakDto2.ar, sakDto2.mnd, 1).toString())
        assertThat(sak2.status).isEqualToIgnoringCase(sakDto2.status.toString())
        assertThat(sak2.beskrivelse).isEqualTo(sakDto2.vedtak?.beskrivelse)
        assertThat(sak2.vedtaksstatus.value()).isEqualTo(sakDto2.vedtak?.type)
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isFalse
    }

    @Test
    fun updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.empty()

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker).isEmpty()
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isTrue
    }

    @Test
    fun updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil_ogBeholderGamleData() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val opplysninger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger
        opplysninger.bostotte.saker.add(
            JsonBostotteSak()
                .withType(SoknadJsonTyper.UTBETALING_HUSBANKEN)
                .withKilde(JsonKildeSystem.SYSTEM)
                .withStatus(BostotteStatus.UNDER_BEHANDLING.toString())
        )
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.empty()

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = opplysninger.bostotte.saker
        assertThat(saker).hasSize(1)
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isTrue
    }

    @Test
    fun updateSystemdata_saker_henterIkkeBostotteUtenSamtykke() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, false)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker).isEmpty()
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isFalse
    }

    @Test
    fun updateSystemdata_saker_fjernerGammelBostotteNarViIkkeHarSamtykke() {
        // Variabler:
        val soknadUnderArbeid1 = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid1.jsonInternalSoknad, true)
        val sakDto = lagSak(
            LocalDate.now().withDayOfMonth(1),
            BostotteStatus.UNDER_BEHANDLING,
            BostotteRolle.HOVEDPERSON,
            null,
            null,
            null
        )
        val bostotteDto = BostotteDto(listOf(sakDto), emptyList())

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid1, "")
        val saker1 = soknadUnderArbeid1.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker1).hasSize(1)

        // Kjøring:
        settBostotteSamtykkePaSoknad(soknadUnderArbeid1.jsonInternalSoknad, false)
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid1, "")
        val saker2 = soknadUnderArbeid1.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker2).isEmpty()
    }

    @Test
    fun updateSystemdata_saker_bipersonerBlirFiltrertBort() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val sakDto1 = lagSak(
            LocalDate.now().withDayOfMonth(1),
            BostotteStatus.UNDER_BEHANDLING,
            BostotteRolle.HOVEDPERSON,
            null,
            null,
            null
        )
        val sakDto2 = lagSak(
            LocalDate.now().withDayOfMonth(1),
            BostotteStatus.VEDTATT,
            BostotteRolle.BIPERSON,
            "V02",
            "Avslag - For høy inntekt",
            Vedtaksstatus.AVSLAG
        )
        val bostotteDto = BostotteDto(listOf(sakDto1, sakDto2), emptyList())

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker).hasSize(1)
        val sak1 = saker[0]
        assertThat(sak1.status).isEqualToIgnoringCase(sakDto1.status.toString())
    }

    @Test
    fun updateSystemdata_utbetalinger_bipersonerBlirFiltrertBort() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val utbetalingDto1 = UtbetalingDto(
            LocalDate.now().minusDays(32),
            BigDecimal.valueOf(10000),
            BostotteMottaker.KOMMUNE,
            BostotteRolle.HOVEDPERSON
        )
        val utbetalingDto2 = UtbetalingDto(
            LocalDate.now().minusDays(32),
            BigDecimal.valueOf(20000),
            BostotteMottaker.HUSSTAND,
            BostotteRolle.BIPERSON
        )
        val bostotteDto = BostotteDto(emptyList(), listOf(utbetalingDto1, utbetalingDto2))

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetalinger).hasSize(1)
        val utbetaling = utbetalinger[0]
        assertThat(utbetaling.netto).isEqualTo(utbetalingDto1.belop.toLong().toDouble())
    }

    @Test
    fun updateSystemdata_bareDataFraSisteManedBlirVist() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val sakDto1 = lagSak(
            LocalDate.now().withDayOfMonth(1),
            BostotteStatus.UNDER_BEHANDLING,
            BostotteRolle.HOVEDPERSON,
            null,
            null,
            null
        )
        val sakDto2 = lagSak(
            LocalDate.now().withDayOfMonth(1).minusDays(32),
            BostotteStatus.VEDTATT,
            BostotteRolle.HOVEDPERSON,
            "V02",
            "Avslag - For høy inntekt",
            Vedtaksstatus.AVSLAG
        )
        val bostotteDto = BostotteDto(listOf(sakDto1, sakDto2), emptyList())

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker).hasSize(1)
        val sak1 = saker[0]
        assertThat(sak1.status).isEqualToIgnoringCase(sakDto1.status.toString())
    }

    @Test
    fun updateSystemdata_dataFraDeSisteToManederBlirVistNarSisteManedErTom() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, true)
        val testDate: LocalDate
        testDate = if (LocalDate.now().dayOfMonth >= 30) {
            LocalDate.now().withDayOfMonth(1)
        } else {
            LocalDate.now().withDayOfMonth(1).minusMonths(1)
        }
        val sakDto2 = lagSak(
            testDate,
            BostotteStatus.VEDTATT,
            BostotteRolle.HOVEDPERSON,
            "V02",
            "Avslag - For høy inntekt",
            Vedtaksstatus.AVSLAG
        )
        val bostotteDto = BostotteDto(listOf(sakDto2), emptyList())

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val saker = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker
        assertThat(saker).hasSize(1)
        val sak1 = saker[0]
        assertThat(sak1.status).isEqualToIgnoringCase(sakDto2.status.toString())
    }

    @Test
    fun updateSystemdata_utbetalinger_henterIkkeBostotteUtenSamtykke() {
        // Variabler:
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid.jsonInternalSoknad, false)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad
            .data.okonomi.opplysninger.utbetaling.stream()
            .filter { utbetaling: JsonOkonomiOpplysningUtbetaling -> utbetaling.kilde == JsonKilde.SYSTEM }
            .collect(Collectors.toList())
        assertThat(utbetalinger).isEmpty()
        assertThat(soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.stotteFraHusbankenFeilet).isFalse
    }

    @Test
    fun updateSystemdata_utbetalinger_fjernerGammelBostotteNarViIkkeHarSamtykke() {
        // Variabler:
        val soknadUnderArbeid1 = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        settBostotteSamtykkePaSoknad(soknadUnderArbeid1.jsonInternalSoknad, true)
        val utbetalingDto = UtbetalingDto(
            LocalDate.now().minusDays(32),
            BigDecimal.valueOf(10000),
            BostotteMottaker.KOMMUNE,
            BostotteRolle.HOVEDPERSON
        )
        val bostotteDto = BostotteDto(emptyList(), listOf(utbetalingDto))

        // Mock:
        every { husbankenClient.hentBostotte(any(), any(), any()) } returns Optional.of(bostotteDto)

        // Kjøring:
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid1, "")
        val utbetalinger1 = soknadUnderArbeid1.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetalinger1).hasSize(1)

        // Kjøring:
        settBostotteSamtykkePaSoknad(soknadUnderArbeid1.jsonInternalSoknad, false)
        bostotteSystemdata.updateSystemdataIn(soknadUnderArbeid1, "")
        val utbetalinger2 = soknadUnderArbeid1.jsonInternalSoknad.soknad
            .data.okonomi.opplysninger.utbetaling.stream()
            .filter { utbetaling: JsonOkonomiOpplysningUtbetaling -> utbetaling.kilde == JsonKilde.SYSTEM }
            .collect(Collectors.toList())
        assertThat(utbetalinger2).isEmpty()
    }

    private fun lagSak(
        saksDato: LocalDate,
        status: BostotteStatus,
        rolle: BostotteRolle,
        kode: String?,
        beskrivelse: String?,
        vedtaksstatus: Vedtaksstatus?
    ): SakDto {
        var vedtakDto: VedtakDto? = null
        if (kode != null) {
            vedtakDto = VedtakDto(kode, beskrivelse ?: "", vedtaksstatus.toString())
        }
        return SakDto(saksDato.monthValue, saksDato.year, status, vedtakDto, rolle)
    }

    private fun settBostotteSamtykkePaSoknad(jsonInternalSoknad: JsonInternalSoknad, harSamtykke: Boolean) {
        val bekreftelser = jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        bekreftelser.removeIf { bekreftelse: JsonOkonomibekreftelse ->
            bekreftelse.type.equals(SoknadJsonTyper.BOSTOTTE_SAMTYKKE, ignoreCase = true)
        }
        bekreftelser
            .add(
                JsonOkonomibekreftelse().withKilde(JsonKilde.SYSTEM)
                    .withType(SoknadJsonTyper.BOSTOTTE_SAMTYKKE)
                    .withVerdi(harSamtykke)
                    .withTittel("beskrivelse")
            )
    }

    private fun assertThatUtbetalingErKorrekt(
        mottaker: BostotteMottaker,
        netto: BigDecimal,
        utbetaling: JsonOkonomiOpplysningUtbetaling,
        utbetalingsDato: LocalDate
    ) {
        assertThat(utbetaling.tittel).isEqualToIgnoringCase("Statlig bostøtte")
        assertThat(utbetaling.mottaker).isEqualTo(JsonOkonomiOpplysningUtbetaling.Mottaker.fromValue(mottaker.value))
        assertThat(utbetaling.type).isEqualTo(SoknadJsonTyper.UTBETALING_HUSBANKEN)
        assertThat(utbetaling.utbetalingsdato).isEqualTo(utbetalingsDato.toString())
        assertThat(utbetaling.netto).isEqualTo(netto.toDouble())
        assertThat(utbetaling.kilde).isEqualTo(JsonKilde.SYSTEM)
    }

    companion object {
        private const val EIER = "12345678910"
    }
}

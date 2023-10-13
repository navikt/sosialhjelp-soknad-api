package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class SkatteetatenSystemdataTest {
    private val organisasjonService: OrganisasjonService = mockk()
    private val skattbarInntektService: SkattbarInntektService = mockk()
    private val skatteetatenSystemdata = SkatteetatenSystemdata(skattbarInntektService, organisasjonService, mockk())

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        every {
            organisasjonService.mapToJsonOrganisasjon(ORGANISASJONSNR)
        } returns JsonOrganisasjon().withOrganisasjonsnummer(ORGANISASJONSNR)
        every {
            organisasjonService.mapToJsonOrganisasjon(ORGANISASJONSNR_ANNEN)
        } returns JsonOrganisasjon().withOrganisasjonsnummer(ORGANISASJONSNR_ANNEN)
        every { organisasjonService.mapToJsonOrganisasjon(PERSONNR) } returns null
    }

    @Test
    fun skalOppdatereUtbetalinger() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad!!, true)
        val skattbareUtbetalinger = listOf(SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_ANNEN)
        every { skattbarInntektService.hentUtbetalinger(any()) } returns skattbareUtbetalinger

        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonUtbetalinger = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling
        val utbetaling = jsonUtbetalinger[0]
        val utbetaling1 = jsonUtbetalinger[1]
        assertThat(utbetaling.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING, utbetaling, UTBETALING_SKATTEETATEN)
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling1, UTBETALING_SKATTEETATEN)
    }

    @Test
    fun skalKunInkludereGyldigeOrganisasjonsnummer() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad!!, true)
        val skattbareUtbetalinger =
            listOf(SKATTBAR_UTBETALING_ANNEN, SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_FRA_PRIVATPERSON)
        every { skattbarInntektService.hentUtbetalinger(any()) } returns skattbareUtbetalinger
        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)
        val jsonUtbetalinger = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling
        val utbetaling = jsonUtbetalinger[0]
        val utbetaling1 = jsonUtbetalinger[1]
        val utbetaling2 = jsonUtbetalinger[2]
        assertThat(jsonUtbetalinger).hasSize(3)
        assertThat(utbetaling.organisasjon.organisasjonsnummer).isEqualTo(ORGANISASJONSNR_ANNEN)
        assertThat(utbetaling1.organisasjon.organisasjonsnummer).isEqualTo(ORGANISASJONSNR)
        assertThat(utbetaling2.organisasjon).isNull()
    }

    @Test
    fun skalOppdatereUtbetalingerUtenAAOverskriveBrukerUtfylteUtbetalinger() {
        val soknadUnderArbeid = createSoknadUnderArbeid(createJsonInternalSoknadWithUtbetalinger())
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad!!, true)
        val utbetalinger = listOf(SKATTBAR_UTBETALING_ANNEN)
        every { skattbarInntektService.hentUtbetalinger(any()) } returns utbetalinger

        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonUtbetalinger = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling
        val utbetaling = jsonUtbetalinger[0]
        val utbetaling1 = jsonUtbetalinger[1]
        assertThat(utbetaling.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING)
        assertThat(utbetaling1.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling1, UTBETALING_SKATTEETATEN)
    }

    @Test
    fun skalIkkeHenteUtbetalingerUtenSamtykke() {
        val soknadUnderArbeid = createSoknadUnderArbeid(createJsonInternalSoknadWithUtbetalinger())
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad!!, false)

        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonUtbetalinger = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling
        val utbetaling = jsonUtbetalinger[0]
        assertThat(utbetaling.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING)
        assertThat(jsonUtbetalinger).hasSize(1)
        assertThat(soknadUnderArbeid.jsonInternalSoknad!!.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet).isFalse
    }

    @Test
    fun skalFjerneUtbetalingerNarViIkkeHarSamtykke() {
        val soknadUnderArbeid = createSoknadUnderArbeid(createJsonInternalSoknadWithUtbetalinger())
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad!!, true)
        val utbetalinger = listOf(SKATTBAR_UTBETALING_ANNEN)
        every { skattbarInntektService.hentUtbetalinger(any()) } returns utbetalinger

        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonUtbetalingerA = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling
        val utbetalingA = jsonUtbetalingerA[0]
        val utbetalingA1 = jsonUtbetalingerA[1]

        // SJEKK STATE FOR TEST:
        assertThat(utbetalingA.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetalingA).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING)
        assertThat(utbetalingA1.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetalingA1, UTBETALING_SKATTEETATEN)

        // TEST:
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad!!, false)
        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)
        val jsonUtbetalingerB = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling
        val utbetalingB = jsonUtbetalingerB[0]
        assertThat(utbetalingB.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetalingB).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING)
        assertThat(jsonUtbetalingerB).hasSize(1)
    }

    @Test
    fun updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil_ogBeholderGamleData() {
        // Variabler:
        val soknadUnderArbeid = createSoknadUnderArbeid()
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad!!, true)
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling
        utbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING)

        // Mock:
        every { skattbarInntektService.hentUtbetalinger(any()) } returns null

        // Kjøring:
        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)
        assertThat(utbetalinger).hasSize(1)
        val utbetaling = utbetalinger[0]
        assertThat(utbetaling.kilde).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.kilde)
        assertThat(utbetaling.type).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.type)
        assertThat(utbetaling.belop).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.belop)
        assertThat(soknadUnderArbeid.jsonInternalSoknad!!.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet).isTrue
    }

    private fun createJsonInternalSoknadWithUtbetalinger(): JsonInternalSoknad {
        val jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)
        val jsonUtbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling> = ArrayList()
        setSamtykke(jsonInternalSoknad, true)
        jsonUtbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING)
        jsonInternalSoknad.soknad.data.okonomi.opplysninger.withUtbetaling(jsonUtbetalinger)
        return jsonInternalSoknad
    }

    private fun setSamtykke(jsonInternalSoknad: JsonInternalSoknad, harSamtykke: Boolean) {
        val bekreftelser = jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        bekreftelser.removeIf { it.type.equals(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) }
        bekreftelser
            .add(
                JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.SYSTEM)
                    .withType(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
                    .withVerdi(harSamtykke)
                    .withTittel("beskrivelse")
            )
    }

    private fun assertThatUtbetalingIsCorrectlyConverted(
        utbetaling: Utbetaling,
        jsonUtbetaling: JsonOkonomiOpplysningUtbetaling,
        type: String
    ) {
        // todo belop, netto og andretrekk er null, men assertion gir NPE?
        assertThat(jsonUtbetaling.type).isEqualTo(type)
        assertThat(jsonUtbetaling.tittel).isEqualTo(utbetaling.tittel)
//        assertThat(jsonUtbetaling.belop).isNull()
        assertThat(jsonUtbetaling.brutto).isEqualTo(utbetaling.brutto)
//        assertThat(jsonUtbetaling.netto).isNull()
        assertThat(jsonUtbetaling.utbetalingsdato).isNull()
        assertThat(jsonUtbetaling.periodeFom)
            .isEqualTo(utbetaling.periodeFom.toString())
        assertThat(jsonUtbetaling.periodeTom)
            .isEqualTo(utbetaling.periodeTom.toString())
        assertThat(jsonUtbetaling.skattetrekk).isEqualTo(utbetaling.skattetrekk)
//        assertThat(jsonUtbetaling.andreTrekk).isNull()
        assertThat(jsonUtbetaling.overstyrtAvBruker).isFalse
    }

    companion object {
        private const val EIER = "12345678901"
        private val JSON_OKONOMI_OPPLYSNING_UTBETALING = JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.BRUKER)
            .withType("Vaffelsalg")
            .withBelop(1000000)
        private val PERIODE_FOM = LocalDate.now().minusDays(40)
        private val PERIODE_TOM = LocalDate.now().minusDays(10)
        private const val TITTEL = "Onkel Skrue penger"
        private const val BRUTTO = 3880.0
        private const val SKATT = -1337.0
        private const val TITTEL_2 = "Lønnsinntekt"
        private const val BRUTTO_2 = 12500.0
        private const val SKATT_2 = -2500.0
        private const val ORGANISASJONSNR = "012345678"
        private const val ORGANISASJONSNR_ANNEN = "999888777"
        private const val PERSONNR = "01010011111"
        private val SKATTBAR_UTBETALING_ANNEN =
            Utbetaling("skatteopplysninger", BRUTTO, SKATT, PERIODE_FOM, PERIODE_TOM, TITTEL, ORGANISASJONSNR_ANNEN)
        private val SKATTBAR_UTBETALING =
            Utbetaling("skatteopplysninger", BRUTTO_2, SKATT_2, PERIODE_FOM, PERIODE_TOM, TITTEL_2, ORGANISASJONSNR)
        private val SKATTBAR_UTBETALING_FRA_PRIVATPERSON =
            Utbetaling("skatteopplysninger", BRUTTO_2, SKATT_2, PERIODE_FOM, PERIODE_TOM, TITTEL_2, PERSONNR)

        private fun createSoknadUnderArbeid(jsonInternalSoknad: JsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "BEHANDLINGSID",
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = jsonInternalSoknad,
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}

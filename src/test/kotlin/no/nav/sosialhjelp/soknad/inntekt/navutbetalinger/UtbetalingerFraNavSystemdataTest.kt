package no.nav.sosialhjelp.soknad.inntekt.navutbetalinger

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class UtbetalingerFraNavSystemdataTest {

    private val organisasjonService: OrganisasjonService = mockk()
    private val navUtbetalingerService: NavUtbetalingerService = mockk()

    private val utbetalingerFraNavSystemdata = UtbetalingerFraNavSystemdata(organisasjonService, navUtbetalingerService)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        every { organisasjonService.hentOrgNavn(any()) } returns "orgnavn"
    }

    @Test
    fun skalOppdatereUtbetalinger() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        val navUtbetalinger = listOf(NAV_UTBETALING_1, NAV_UTBETALING_2)
        every { navUtbetalingerService.getUtbetalingerSiste40Dager(any()) } returns navUtbetalinger

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonUtbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        val utbetaling = jsonUtbetalinger[0]
        val utbetaling1 = jsonUtbetalinger[1]
        assertThat(utbetaling.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_1, utbetaling, SoknadJsonTyper.UTBETALING_NAVYTELSE)
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_2, utbetaling1, SoknadJsonTyper.UTBETALING_NAVYTELSE)
    }

    @Test
    fun skalKunInkludereGyldigeOrganisasjonsnummer() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        val navUtbetalinger = listOf(NAV_UTBETALING_1, NAV_UTBETALING_2, NAV_UTBETALING_3)
        every { navUtbetalingerService.getUtbetalingerSiste40Dager(any()) } returns navUtbetalinger

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonUtbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        val utbetaling = jsonUtbetalinger[0]
        val utbetaling1 = jsonUtbetalinger[1]
        val utbetaling2 = jsonUtbetalinger[2]
        assertThat(jsonUtbetalinger).hasSize(3)
        assertThat(utbetaling.organisasjon).isNull()
        assertThat(utbetaling1.organisasjon.organisasjonsnummer).isEqualTo(ORGANISASJONSNR)
        assertThat(utbetaling2.organisasjon).isNull()
    }

    @Test
    fun skalIkksLasteNedUtbetalingerUtenSamtykke() {
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger())
        val utbetalinger = listOf(NAV_UTBETALING_1)
        every { navUtbetalingerService.getUtbetalingerSiste40Dager(any()) } returns utbetalinger

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonUtbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        val utbetaling = jsonUtbetalinger[0]
        val utbetaling1 = jsonUtbetalinger[1]
        assertThat(utbetaling.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING)
        assertThat(utbetaling1.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_1, utbetaling1, SoknadJsonTyper.UTBETALING_NAVYTELSE)
    }

    @Test
    fun skalReturnereOrganisasjonOmGyldigOrganisasjonsnummer() {
        val organisasjonsnummer = "089640782"
        val result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(organisasjonsnummer)
        assertThat(result).isNotNull
        assertThat(result!!.organisasjonsnummer).isEqualTo(organisasjonsnummer)
    }

    @Test
    fun skalReturnereNullOmOrganisasjonsnummerInneholderTekst() {
        val organisasjonsnummer = "o89640782"
        val result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(organisasjonsnummer)
        assertThat(result).isNull()
    }

    @Test
    fun skalReturnereNullOmForKortOrganisasjonsnummer() {
        val nummer = "12345678"
        val result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(nummer)
        assertThat(result).isNull()
    }

    @Test
    fun skalReturnereNullOmForLangtOrganisasjonsnummer() {
        val nummer = "1234567890"
        val result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(nummer)
        assertThat(result).isNull()
    }

    @Test
    fun skalReturnereOrganisasjonUtenNummerVedPersonnummer() {
        val personnummer = "01010011111"
        val result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(personnummer)
        assertThat(result).isNull()
    }

    private fun createJsonInternalSoknadWithUtbetalinger(): JsonInternalSoknad {
        val jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad(EIER)
        val jsonUtbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling> = ArrayList()
        jsonUtbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING)
        jsonInternalSoknad.soknad.data.okonomi.opplysninger.withUtbetaling(jsonUtbetalinger)
        return jsonInternalSoknad
    }

    private fun assertThatUtbetalingIsCorrectlyConverted(
        navUtbetaling: NavUtbetaling,
        jsonUtbetaling: JsonOkonomiOpplysningUtbetaling,
        type: String
    ) {
        assertThat(jsonUtbetaling.type).isEqualTo(type)
        assertThat(jsonUtbetaling.tittel).isEqualTo(navUtbetaling.tittel)
        assertThat(jsonUtbetaling.belop)
            .isEqualTo(UtbetalingerFraNavSystemdata.tilIntegerMedAvrunding(navUtbetaling.netto.toString()))
        assertThat(jsonUtbetaling.brutto).isEqualTo(navUtbetaling.brutto)
        assertThat(jsonUtbetaling.netto).isEqualTo(navUtbetaling.netto)
        assertThat(jsonUtbetaling.utbetalingsdato).isEqualTo(navUtbetaling.utbetalingsdato.toString())
        assertThat(jsonUtbetaling.periodeFom).isEqualTo(navUtbetaling.periodeFom.toString())
        assertThat(jsonUtbetaling.periodeTom).isEqualTo(navUtbetaling.periodeTom.toString())
        assertThat(jsonUtbetaling.skattetrekk).isEqualTo(navUtbetaling.skattetrekk)
        assertThat(jsonUtbetaling.andreTrekk).isEqualTo(navUtbetaling.andreTrekk)
        assertThat(jsonUtbetaling.overstyrtAvBruker).isFalse
        if (!navUtbetaling.komponenter.isEmpty()) {
            for (i in navUtbetaling.komponenter.indices) {
                val (type1, belop, satsType, satsBelop, satsAntall) = navUtbetaling.komponenter[i]
                val jsonKomponent = jsonUtbetaling.komponenter[i]
                assertThat(jsonKomponent.type).isEqualTo(type1)
                assertThat(jsonKomponent.belop).isEqualTo(belop)
                assertThat(jsonKomponent.satsType).isEqualTo(satsType)
                assertThat(jsonKomponent.satsAntall).isEqualTo(satsAntall)
                assertThat(jsonKomponent.satsBelop).isEqualTo(satsBelop)
            }
        }
    }

    companion object {
        private const val EIER = "12345678901"
        private val JSON_OKONOMI_OPPLYSNING_UTBETALING = JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.BRUKER)
            .withType("Vaffelsalg")
            .withBelop(1000000)
        private val UTBETALINGSDATO = LocalDate.now().minusDays(5)
        private val PERIODE_FOM = LocalDate.now().minusDays(40)
        private val PERIODE_TOM = LocalDate.now().minusDays(10)
        private const val TITTEL = "Onkel Skrue penger"
        private const val NETTO = 60000.0
        private const val BRUTTO = 3880.0
        private const val SKATT = -1337.0
        private const val TREKK = -500.0
        private const val KOMPONENTTYPE = "Pengesekk"
        private const val KOMPONENTBELOP = 50000.0
        private const val SATSTYPE = "Dag"
        private const val SATSBELOP = 5000.0
        private const val SATSANTALL = 10.0
        private const val TITTEL_2 = "Lønnsinntekt"
        private const val NETTO_2 = 10000.0
        private const val BRUTTO_2 = 12500.0
        private const val SKATT_2 = -2500.0
        private const val TREKK_2 = 0.0
        private const val ORGANISASJONSNR = "012345678"
        private const val PERSONNR = "01010011111"
        private val NAV_KOMPONENT = Komponent(KOMPONENTTYPE, KOMPONENTBELOP, SATSTYPE, SATSBELOP, SATSANTALL)
        private val NAV_UTBETALING_1 = NavUtbetaling(
            "type", NETTO, BRUTTO, SKATT, TREKK, null, UTBETALINGSDATO, PERIODE_FOM, PERIODE_TOM,
            listOf(
                NAV_KOMPONENT
            ),
            TITTEL, "orgnr"
        )
        private val NAV_UTBETALING_2 = NavUtbetaling(
            "type",
            NETTO_2,
            BRUTTO_2,
            SKATT_2,
            TREKK_2,
            null,
            UTBETALINGSDATO,
            PERIODE_FOM,
            PERIODE_TOM,
            emptyList(),
            TITTEL_2,
            ORGANISASJONSNR
        )
        private val NAV_UTBETALING_3 = NavUtbetaling(
            "type", NETTO_2, BRUTTO_2, SKATT_2, TREKK_2, null, UTBETALINGSDATO, PERIODE_FOM, PERIODE_TOM,
            listOf(
                NAV_KOMPONENT
            ),
            TITTEL_2, PERSONNR
        )
    }
}

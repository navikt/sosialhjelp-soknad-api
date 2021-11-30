package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NavUtbetalingerTest {

    private val navUtbetalinger = NavUtbetalinger()

    @Test
    fun hentingFeilet() {
        val opplysninger = JsonOkonomiopplysninger()
        val driftsinformasjon = JsonDriftsinformasjon().withUtbetalingerFraNavFeilet(true)

        val avsnitt = navUtbetalinger.getAvsnitt(opplysninger, driftsinformasjon)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val sporsmal = avsnitt.sporsmal[0]
        assertThat(sporsmal.tittel).isEqualTo("utbetalinger.kontaktproblemer")
        assertThat(sporsmal.erUtfylt).isTrue
    }

    @Test
    fun ingenNavUtbetalinger() {
        val opplysninger = JsonOkonomiopplysninger().withUtbetaling(emptyList())

        val avsnitt = navUtbetalinger.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(1)

        val sporsmal = avsnitt.sporsmal[0]
        assertThat(sporsmal.tittel).isEqualTo("utbetalinger.ingen.true")
        assertThat(sporsmal.erUtfylt).isTrue
        assertThat(sporsmal.felt).isNull()
    }

    @Test
    fun flereNavUtbetalinger() {
        val opplysninger = JsonOkonomiopplysninger()
            .withUtbetaling(
                listOf(
                    createUtbetaling("Dagpenger", 1234.0, "2021-01-01"),
                    createUtbetaling("Uføre", 42.0, "2021-03-03")
                )
            )

        val avsnitt = navUtbetalinger.getAvsnitt(opplysninger, JsonDriftsinformasjon())
        assertThat(avsnitt.sporsmal).hasSize(2)

        val sporsmalUtbetaling1 = avsnitt.sporsmal[0]
        assertThat(sporsmalUtbetaling1.tittel).isEqualTo("utbetalinger.utbetaling.sporsmal")
        assertThat(sporsmalUtbetaling1.erUtfylt).isTrue
        assertThat(sporsmalUtbetaling1.felt).hasSize(1)
        assertThat(sporsmalUtbetaling1.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap1 = sporsmalUtbetaling1.felt!![0].labelSvarMap
        assertThat(labelSvarMap1).hasSize(4)
        assertThat(labelSvarMap1!!["utbetalinger.utbetaling.type.label"]!!.value).isEqualTo("Dagpenger")
        assertThat(labelSvarMap1["utbetalinger.utbetaling.netto.label"]!!.value).isEqualTo("1234.0")
        assertThat(labelSvarMap1["utbetalinger.utbetaling.brutto.label"]!!.value).isEqualTo("2234.0")
        assertThat(labelSvarMap1["utbetalinger.utbetaling.utbetalingsdato.label"]!!.value).isEqualTo("2021-01-01")

        val sporsmalUtbetaling2 = avsnitt.sporsmal[1]
        assertThat(sporsmalUtbetaling2.tittel).isEqualTo("utbetalinger.utbetaling.sporsmal")
        assertThat(sporsmalUtbetaling2.erUtfylt).isTrue
        assertThat(sporsmalUtbetaling2.felt).hasSize(1)
        assertThat(sporsmalUtbetaling2.felt!![0].type).isEqualTo(Type.SYSTEMDATA_MAP)

        val labelSvarMap2 = sporsmalUtbetaling2.felt!![0].labelSvarMap
        assertThat(labelSvarMap2).hasSize(4)
        assertThat(labelSvarMap2!!["utbetalinger.utbetaling.type.label"]!!.value).isEqualTo("Uføre")
        assertThat(labelSvarMap2["utbetalinger.utbetaling.netto.label"]!!.value).isEqualTo("42.0")
        assertThat(labelSvarMap2["utbetalinger.utbetaling.brutto.label"]!!.value).isEqualTo("1042.0")
        assertThat(labelSvarMap2["utbetalinger.utbetaling.utbetalingsdato.label"]!!.value).isEqualTo("2021-03-03")
    }

    private fun createUtbetaling(
        tittel: String,
        netto: Double,
        utbetalingsdato: String
    ): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling()
            .withType(SoknadJsonTyper.UTBETALING_NAVYTELSE)
            .withKilde(JsonKilde.SYSTEM)
            .withTittel(tittel)
            .withNetto(netto)
            .withBrutto(netto + 1000)
            .withUtbetalingsdato(utbetalingsdato)
    }
}

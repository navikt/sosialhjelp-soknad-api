package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SkattbarInntektTest {
    private val skattbarInntekt = SkattbarInntekt()

    @Test
    fun manglerSamtykke() {
        val okonomi = createOkonomi(false)
        val driftsinformasjon = JsonDriftsinformasjon(false)

        val avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon)
        assertThat(avsnitt.sporsmal).hasSize(1)
        assertThat(avsnitt.sporsmal[0].tittel).isEqualTo("utbetalinger.inntekt.skattbar.mangler_samtykke")
    }

    @Test
    fun feilMotSkatteetaten() {
        val okonomi = createOkonomi(true)
        val driftsinformasjon = JsonDriftsinformasjon(true)

        val avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon)
        assertThat(avsnitt.sporsmal).hasSize(1)
        assertThat(avsnitt.sporsmal[0].tittel).isEqualTo("utbetalinger.skattbar.kontaktproblemer.oppsummering")
    }

    @Test
    fun ingenSkattbareInntekter() {
        val okonomi = createOkonomi(true)
        val driftsinformasjon = JsonDriftsinformasjon(false)

        val avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon)
        assertThat(avsnitt.sporsmal).hasSize(2)

        val bekreftelseTidspunktSporsmal = avsnitt.sporsmal[0]
        assertThat(bekreftelseTidspunktSporsmal.tittel).isEqualTo("utbetalinger.inntekt.skattbar.har_gitt_samtykke")
        assertThat(bekreftelseTidspunktSporsmal.erUtfylt).isTrue
        assertThat(bekreftelseTidspunktSporsmal.felt).hasSize(1)
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.felt!![0], Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z")

        val inntekterSporsmal = avsnitt.sporsmal[1]
        assertThat(inntekterSporsmal.tittel).isEqualTo("utbetalinger.inntekt.skattbar.inntekt.tittel")
        assertThat(inntekterSporsmal.erUtfylt).isTrue
        assertThat(inntekterSporsmal.felt).hasSize(1)
        validateFeltMedSvar(inntekterSporsmal.felt!![0], Type.TEKST, SvarType.LOCALE_TEKST, "utbetalinger.inntekt.skattbar.ingen")
    }

    @Test
    fun harEnSkattbarInntekt() {
        val okonomi = createOkonomi(true).copy(opplysninger = createOkonomi(true).opplysninger.copy(utbetaling = listOf(createUtbetaling("2020-01-01", "2020-02-01", 1234.0, 123.0))))
        val driftsinformasjon = JsonDriftsinformasjon(false)

        val avsnitt = skattbarInntekt.getAvsnitt(okonomi, driftsinformasjon)
        assertThat(avsnitt.sporsmal).hasSize(2)

        val bekreftelseTidspunktSporsmal = avsnitt.sporsmal[0]
        assertThat(bekreftelseTidspunktSporsmal.tittel).isEqualTo("utbetalinger.inntekt.skattbar.har_gitt_samtykke")
        assertThat(bekreftelseTidspunktSporsmal.erUtfylt).isTrue
        assertThat(bekreftelseTidspunktSporsmal.felt).hasSize(1)
        validateFeltMedSvar(bekreftelseTidspunktSporsmal.felt!![0], Type.TEKST, SvarType.TIDSPUNKT, "2018-10-04T13:37:00.134Z")

        val inntekterSporsmal = avsnitt.sporsmal[1]
        assertThat(inntekterSporsmal.tittel).isEqualTo("utbetalinger.inntekt.skattbar.inntekt.tittel")
        assertThat(inntekterSporsmal.erUtfylt).isTrue
        assertThat(inntekterSporsmal.felt).hasSize(1)

        val inntekt = inntekterSporsmal.felt!![0]
        assertThat(inntekt.type).isEqualTo(Type.SYSTEMDATA_MAP)
        assertThat(inntekt.labelSvarMap).hasSize(5)
        assertThat(inntekt.labelSvarMap!!["utbetalinger.utbetaling.arbeidsgivernavn.label"]!!.value).isEqualTo("arbeidsgiver")
        assertThat(inntekt.labelSvarMap["utbetalinger.utbetaling.periodeFom.label"]!!.value).isEqualTo("2020-01-01")
        assertThat(inntekt.labelSvarMap["utbetalinger.utbetaling.periodeTom.label"]!!.value).isEqualTo("2020-02-01")
        assertThat(inntekt.labelSvarMap["utbetalinger.utbetaling.brutto.label"]!!.value).isEqualTo("1234.0")
        assertThat(inntekt.labelSvarMap["utbetalinger.utbetaling.skattetrekk.label"]!!.value).isEqualTo("123.0")
    }

    private fun createOkonomi(harSamtykke: Boolean): JsonOkonomi {
        return JsonOkonomi(JsonOkonomiopplysninger(emptyList(), listOf(JsonOkonomibekreftelse(JsonKilde.BRUKER, SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE, "", harSamtykke, "2018-10-04T13:37:00.134Z")), null, emptyList(), null), null)
    }

    private fun createUtbetaling(
        fom: String,
        tom: String,
        brutto: Double,
        skattetrekk: Double,
    ): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling(JsonKilde.SYSTEM, SoknadJsonTyper.UTBETALING_SKATTEETATEN, "", false, brutto = brutto, skattetrekk = skattetrekk, periodeFom = fom, periodeTom = tom, organisasjon = JsonOrganisasjon("arbeidsgiver", ""))
    }
}

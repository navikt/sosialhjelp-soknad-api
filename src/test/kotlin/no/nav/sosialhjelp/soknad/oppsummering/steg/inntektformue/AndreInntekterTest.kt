package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AndreInntekterTest {

    private val andreInntekter = AndreInntekter()

    @Test
    fun harIkkeUtfyltSporsmal() {
        val opplysninger = JsonOkonomiopplysninger()
            .withBekreftelse(emptyList())

        val avsnitt = andreInntekter.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val harAndreInntekterSporsmal = avsnitt.sporsmal[0]
        assertThat(harAndreInntekterSporsmal.tittel).isEqualTo("inntekt.inntekter.sporsmal")
        assertThat(harAndreInntekterSporsmal.erUtfylt).isFalse
        assertThat(harAndreInntekterSporsmal.felt).isNull()
    }

    @Test
    fun harIkkeAndreInntekter() {
        val opplysninger = createOpplysninger(false)

        val avsnitt = andreInntekter.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val harAndreInntekterSporsmal = avsnitt.sporsmal[0]
        assertThat(harAndreInntekterSporsmal.tittel).isEqualTo("inntekt.inntekter.sporsmal")
        assertThat(harAndreInntekterSporsmal.erUtfylt).isTrue
        assertThat(harAndreInntekterSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAndreInntekterSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.inntekter.false")
    }

    @Test
    fun harAndreInntekterMenIkkeUtfyltHvaEierDu() {
        val opplysninger = createOpplysninger(true)
        opplysninger.utbetaling = emptyList()

        val avsnitt = andreInntekter.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(2)

        val harAndreInntekterSporsmal = avsnitt.sporsmal[0]
        assertThat(harAndreInntekterSporsmal.tittel).isEqualTo("inntekt.inntekter.sporsmal")
        assertThat(harAndreInntekterSporsmal.erUtfylt).isTrue
        assertThat(harAndreInntekterSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAndreInntekterSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.inntekter.true")

        val hvaHarDuMottattSporsmal = avsnitt.sporsmal[1]
        assertThat(hvaHarDuMottattSporsmal.tittel).isEqualTo("inntekt.inntekter.true.type.sporsmal")
        assertThat(hvaHarDuMottattSporsmal.erUtfylt).isFalse
        assertThat(hvaHarDuMottattSporsmal.felt).isNull()
    }

    @Test
    fun harAndreInntekterMedUtbetalinger() {
        val opplysninger = createOpplysninger(true)
        opplysninger.utbetaling = listOf(
            createUtbetaling(SoknadJsonTyper.UTBETALING_UTBYTTE),
            createUtbetaling(SoknadJsonTyper.UTBETALING_SALG)
        )

        val avsnitt = andreInntekter.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(2)

        val harAndreInntekterSporsmal = avsnitt.sporsmal[0]
        assertThat(harAndreInntekterSporsmal.tittel).isEqualTo("inntekt.inntekter.sporsmal")
        assertThat(harAndreInntekterSporsmal.erUtfylt).isTrue
        assertThat(harAndreInntekterSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAndreInntekterSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.inntekter.true")

        val hvaHarDuMottattSporsmal = avsnitt.sporsmal[1]
        assertThat(hvaHarDuMottattSporsmal.tittel).isEqualTo("inntekt.inntekter.true.type.sporsmal")
        assertThat(hvaHarDuMottattSporsmal.erUtfylt).isTrue
        assertThat(hvaHarDuMottattSporsmal.felt).hasSize(2)
        validateFeltMedSvar(hvaHarDuMottattSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "json.okonomi.opplysninger.inntekt.inntekter.utbytte")
        validateFeltMedSvar(hvaHarDuMottattSporsmal.felt!![1], Type.CHECKBOX, SvarType.LOCALE_TEKST, "json.okonomi.opplysninger.inntekt.inntekter.salg")
    }

    @Test
    fun harAndreInntekterUtenBeskrivelseAvAnnet() {
        val opplysninger = createOpplysninger(true)
        opplysninger.utbetaling = listOf(
            createUtbetaling(SoknadJsonTyper.UTBETALING_ANNET)
        )

        val avsnitt = andreInntekter.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(3)

        val harAndreInntekterSporsmal = avsnitt.sporsmal[0]
        assertThat(harAndreInntekterSporsmal.tittel).isEqualTo("inntekt.inntekter.sporsmal")
        assertThat(harAndreInntekterSporsmal.erUtfylt).isTrue
        assertThat(harAndreInntekterSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAndreInntekterSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.inntekter.true")

        val hvaHarDuMottattSporsmal = avsnitt.sporsmal[1]
        assertThat(hvaHarDuMottattSporsmal.tittel).isEqualTo("inntekt.inntekter.true.type.sporsmal")
        assertThat(hvaHarDuMottattSporsmal.erUtfylt).isTrue
        assertThat(hvaHarDuMottattSporsmal.felt).hasSize(1)
        validateFeltMedSvar(hvaHarDuMottattSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "json.okonomi.opplysninger.inntekt.inntekter.annet")

        val annetBeskrivelseSporsmal = avsnitt.sporsmal[2]
        assertThat(annetBeskrivelseSporsmal.tittel).isEqualTo("inntekt.inntekter.true.type.annet")
        assertThat(annetBeskrivelseSporsmal.erUtfylt).isFalse
        assertThat(annetBeskrivelseSporsmal.felt).isNull()
    }

    @Test
    fun harAndreInntekterMedBeskrivelseAvAnnet() {
        val opplysninger = createOpplysninger(true)
        opplysninger.utbetaling = listOf(
            createUtbetaling(SoknadJsonTyper.UTBETALING_ANNET)
        )
        opplysninger.beskrivelseAvAnnet = JsonOkonomibeskrivelserAvAnnet()
            .withUtbetaling("ANNEN")

        val avsnitt = andreInntekter.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(3)

        val harAndreInntekterSporsmal = avsnitt.sporsmal[0]
        assertThat(harAndreInntekterSporsmal.tittel).isEqualTo("inntekt.inntekter.sporsmal")
        assertThat(harAndreInntekterSporsmal.erUtfylt).isTrue
        assertThat(harAndreInntekterSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAndreInntekterSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.inntekter.true")

        val hvaHarDuMottattSporsmal = avsnitt.sporsmal[1]
        assertThat(hvaHarDuMottattSporsmal.tittel).isEqualTo("inntekt.inntekter.true.type.sporsmal")
        assertThat(hvaHarDuMottattSporsmal.erUtfylt).isTrue
        assertThat(hvaHarDuMottattSporsmal.felt).hasSize(1)
        validateFeltMedSvar(hvaHarDuMottattSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "json.okonomi.opplysninger.inntekt.inntekter.annet")

        val annetBeskrivelseSporsmal = avsnitt.sporsmal[2]
        assertThat(annetBeskrivelseSporsmal.tittel).isEqualTo("inntekt.inntekter.true.type.annet")
        assertThat(annetBeskrivelseSporsmal.erUtfylt).isTrue
        assertThat(annetBeskrivelseSporsmal.felt).hasSize(1)
        validateFeltMedSvar(annetBeskrivelseSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "ANNEN")
    }

    private fun createOpplysninger(harBekreftelse: Boolean): JsonOkonomiopplysninger {
        return JsonOkonomiopplysninger()
            .withBekreftelse(
                listOf(
                    JsonOkonomibekreftelse()
                        .withType(SoknadJsonTyper.BEKREFTELSE_UTBETALING)
                        .withVerdi(harBekreftelse)
                )
            )
    }

    private fun createUtbetaling(type: String): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling()
            .withType(type)
            .withKilde(JsonKilde.BRUKER)
    }
}

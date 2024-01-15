package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AnnenFormueTest {

    private val annenFormue = AnnenFormue()

    @Test
    fun ikkeUtfylt() {
        val okonomi = JsonOkonomi()
            .withOpplysninger(
                JsonOkonomiopplysninger()
                    .withBekreftelse(emptyList()),
            )

        val avsnitt = annenFormue.getAvsnitt(okonomi)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val harAnnenFormueSporsmal = avsnitt.sporsmal[0]
        assertThat(harAnnenFormueSporsmal.erUtfylt).isFalse
        assertThat(harAnnenFormueSporsmal.felt).isNull()
    }

    @Test
    fun harIkkeAnnenFormue() {
        val okonomi = createOkonomi(false)

        val avsnitt = annenFormue.getAvsnitt(okonomi)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val harAnnenFormueSporsmal = avsnitt.sporsmal[0]
        assertThat(harAnnenFormueSporsmal.erUtfylt).isTrue
        assertThat(harAnnenFormueSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAnnenFormueSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.false")
    }

    @Test
    fun harAnnenFormueUtenHvaEierDuSvar() {
        val okonomi = createOkonomi(true)
        okonomi.withOversikt(
            JsonOkonomioversikt()
                .withFormue(emptyList()),
        )

        val avsnitt = annenFormue.getAvsnitt(okonomi)
        assertThat(avsnitt.sporsmal).hasSize(2)

        val harAnnenFormueSporsmal = avsnitt.sporsmal[0]
        assertThat(harAnnenFormueSporsmal.erUtfylt).isTrue
        assertThat(harAnnenFormueSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAnnenFormueSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true")

        val hvaEierDuSporsmal = avsnitt.sporsmal[1]
        assertThat(hvaEierDuSporsmal.erUtfylt).isFalse
        assertThat(hvaEierDuSporsmal.felt).isNull()
    }

    @Test
    fun harAnnenFormueMedBeksrivelseAnnet() {
        val okonomi = createOkonomi(true)
        okonomi.withOversikt(
            JsonOkonomioversikt()
                .withFormue(
                    listOf(
                        createFormue(SoknadJsonTyper.VERDI_BOLIG),
                        createFormue(SoknadJsonTyper.VERDI_ANNET),
                    ),
                ),
        )
        okonomi.opplysninger.beskrivelseAvAnnet = JsonOkonomibeskrivelserAvAnnet().withVerdi("verdi")

        val avsnitt = annenFormue.getAvsnitt(okonomi)
        assertThat(avsnitt.sporsmal).hasSize(3)

        val harAnnenFormueSporsmal = avsnitt.sporsmal[0]
        assertThat(harAnnenFormueSporsmal.erUtfylt).isTrue
        assertThat(harAnnenFormueSporsmal.felt).hasSize(1)
        validateFeltMedSvar(harAnnenFormueSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true")

        val hvaEierDuSporsmal = avsnitt.sporsmal[1]
        assertThat(hvaEierDuSporsmal.erUtfylt).isTrue
        assertThat(hvaEierDuSporsmal.felt).hasSize(2)
        validateFeltMedSvar(hvaEierDuSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true.type.bolig")
        validateFeltMedSvar(hvaEierDuSporsmal.felt!![1], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true.type.annet")

        val annetBeskrivelseSporsmal = avsnitt.sporsmal[2]
        assertThat(annetBeskrivelseSporsmal.erUtfylt).isTrue
        assertThat(annetBeskrivelseSporsmal.felt).hasSize(1)
        validateFeltMedSvar(annetBeskrivelseSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "verdi")
    }

    private fun createOkonomi(harBekreftelse: Boolean): JsonOkonomi {
        return JsonOkonomi()
            .withOpplysninger(
                JsonOkonomiopplysninger()
                    .withBekreftelse(
                        listOf(
                            JsonOkonomibekreftelse()
                                .withType(SoknadJsonTyper.BEKREFTELSE_VERDI)
                                .withVerdi(harBekreftelse),
                        ),
                    ),
            )
    }

    private fun createFormue(type: String): JsonOkonomioversiktFormue {
        return JsonOkonomioversiktFormue()
            .withType(type)
            .withKilde(JsonKilde.BRUKER)
    }
}

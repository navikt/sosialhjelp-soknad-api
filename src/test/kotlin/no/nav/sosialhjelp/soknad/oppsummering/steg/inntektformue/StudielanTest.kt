package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StudielanTest {

    private val studielan = Studielan()

    @Test
    fun harIkkeUtfyltSporsmal() {
        val opplysninger = JsonOkonomiopplysninger().withBekreftelse(emptyList())

        val avsnitt = studielan.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val studielanSporsmal = avsnitt.sporsmal[0]
        assertThat(studielanSporsmal.tittel).isEqualTo("inntekt.studielan.sporsmal")
        assertThat(studielanSporsmal.erUtfylt).isFalse
        assertThat(studielanSporsmal.felt).isNull()
    }

    @Test
    fun harSvartJa() {
        val opplysninger = JsonOkonomiopplysninger()
            .withBekreftelse(
                listOf(
                    JsonOkonomibekreftelse()
                        .withType(SoknadJsonTyper.STUDIELAN)
                        .withVerdi(true),
                ),
            )

        val avsnitt = studielan.getAvsnitt(opplysninger)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val studielanSporsmal = avsnitt.sporsmal[0]
        assertThat(studielanSporsmal.tittel).isEqualTo("inntekt.studielan.sporsmal")
        assertThat(studielanSporsmal.erUtfylt).isTrue
        assertThat(studielanSporsmal.felt).hasSize(1)
        validateFeltMedSvar(studielanSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.studielan.true")
    }
}

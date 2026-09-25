package no.nav.sosialhjelp.soknad.oppsummering.steg.inntektformue

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
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

internal class BankTest {
    private val bank = Bank()

    @Test
    fun ikkeUtfylt() {
        val okonomi = JsonOkonomi(opplysninger = JsonOkonomiopplysninger(utbetaling = emptyList()))

        val avsnitt = bank.getAvsnitt(okonomi)
        assertThat(avsnitt.sporsmal).hasSize(1)

        val bankSporsmal = avsnitt.sporsmal[0]
        assertThat(bankSporsmal.erUtfylt).isTrue
        assertThat(bankSporsmal.felt).isNull()
    }

    @Test
    fun valgtFlereBankFormuerMedBeskrivelseAvAnnet() {
        val okonomi =
            createOkonomi(true).copy(
                oversikt = JsonOkonomioversikt(emptyList(), emptyList(), listOf(createFormue(SoknadJsonTyper.FORMUE_BRUKSKONTO), createFormue(SoknadJsonTyper.FORMUE_ANNET))),
                opplysninger =
                    createOkonomi(true).opplysninger.copy(
                        beskrivelseAvAnnet =
                            JsonOkonomibeskrivelserAvAnnet(
                                kilde = JsonKildeBruker.BRUKER,
                                verdi = "",
                                sparing = "sparing",
                                utbetaling = "",
                                boutgifter = "",
                                barneutgifter = "",
                            ),
                    ),
            )

        val avsnitt = bank.getAvsnitt(okonomi)
        assertThat(avsnitt.sporsmal).hasSize(2)

        val bankSporsmal = avsnitt.sporsmal[0]
        assertThat(bankSporsmal.erUtfylt).isTrue
        assertThat(bankSporsmal.felt).hasSize(2)
        validateFeltMedSvar(bankSporsmal.felt!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bankinnskudd.true.type.brukskonto")
        validateFeltMedSvar(bankSporsmal.felt[1], Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bankinnskudd.true.type.annet")

        val beskrivelseAnnetSporsmal = avsnitt.sporsmal[1]
        assertThat(beskrivelseAnnetSporsmal.erUtfylt).isTrue
        assertThat(beskrivelseAnnetSporsmal.felt).hasSize(1)
        validateFeltMedSvar(beskrivelseAnnetSporsmal.felt!![0], Type.TEKST, SvarType.TEKST, "sparing")
    }

    private fun createOkonomi(harBekreftelse: Boolean): JsonOkonomi {
        return JsonOkonomi(
            opplysninger =
                JsonOkonomiopplysninger(
                    utbetaling = emptyList(),
                    bekreftelse = listOf(JsonOkonomibekreftelse(JsonKilde.BRUKER, SoknadJsonTyper.BEKREFTELSE_SPARING, "", harBekreftelse)),
                ),
        )
    }

    private fun createFormue(type: String): JsonOkonomioversiktFormue {
        return JsonOkonomioversiktFormue(kilde = JsonKilde.BRUKER, type = type, tittel = "", overstyrtAvBruker = false)
    }
}

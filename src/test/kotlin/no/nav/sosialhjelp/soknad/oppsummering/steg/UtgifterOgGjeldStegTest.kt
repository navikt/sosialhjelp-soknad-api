package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UtgifterOgGjeldStegTest {
    private val steg = UtgifterOgGjeldSteg

    @Test
    fun boutgifterIkkeUtfylt() {
        val soknad = createSoknad(emptyList(), null, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)

        val utgifterSporsmal = res.avsnitt[0].sporsmal
        assertThat(utgifterSporsmal).hasSize(1)
        assertThat(utgifterSporsmal[0].erUtfylt).isFalse // boutgifter
    }

    @Test
    fun harIkkeBoutgifter() {
        val bekreftelser =
            listOf(
                JsonOkonomibekreftelse(JsonKilde.BRUKER, SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER, "", false),
            )
        val soknad = createSoknad(bekreftelser, null, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)

        val utgifterSporsmal = res.avsnitt[0].sporsmal
        assertThat(utgifterSporsmal).hasSize(1)
        assertThat(utgifterSporsmal[0].erUtfylt).isTrue // boutgifter
    }

    @Test
    fun harBoutgifter() {
        val bekreftelser =
            listOf(
                JsonOkonomibekreftelse(JsonKilde.BRUKER, SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER, "", true),
            )
        val opplysningUtgifter =
            listOf(
                JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_STROM, "", false),
                JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT, "", false),
                JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_OPPVARMING, "", false),
                JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_ANNET_BO, "", false),
            )
        val oversiktUtgifter =
            listOf(
                JsonOkonomioversiktUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_HUSLEIE, "", false),
                JsonOkonomioversiktUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG, "", false),
            )
        val soknad = createSoknad(bekreftelser, opplysningUtgifter, oversiktUtgifter)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)

        val utgifterSporsmal = res.avsnitt[0].sporsmal
        assertThat(utgifterSporsmal).hasSize(2)
        assertThat(utgifterSporsmal[0].erUtfylt).isTrue // boutgifter
        assertThat(utgifterSporsmal[1].erUtfylt).isTrue

        val boutgifterFelter = utgifterSporsmal[1].felt
        assertThat(boutgifterFelter).hasSize(6)
        validateFeltMedSvar(boutgifterFelter!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.husleie")
        validateFeltMedSvar(boutgifterFelter[1], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.strom")
        validateFeltMedSvar(boutgifterFelter[2], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.kommunalAvgift")
        validateFeltMedSvar(boutgifterFelter[3], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.oppvarming")
        validateFeltMedSvar(boutgifterFelter[4], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.boliglanAvdrag")
        validateFeltMedSvar(boutgifterFelter[5], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.annenBoutgift")
    }

    @Test
    fun harIkkeBarneutgifter() {
        val bekreftelser =
            listOf(
                JsonOkonomibekreftelse(JsonKilde.BRUKER, SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER, "", false),
            )
        val soknad = createSoknad(bekreftelser, null, null)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)

        val utgifterSporsmal = res.avsnitt[0].sporsmal
        assertThat(utgifterSporsmal).hasSize(1)
        assertThat(utgifterSporsmal[0].erUtfylt).isFalse // boutgifter
    }

    @Test
    fun harBarneutgifter() {
        val bekreftelser =
            listOf(
                JsonOkonomibekreftelse(JsonKilde.BRUKER, SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER, "", true),
            )
        val opplysningUtgifter =
            listOf(
                JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER, "", false),
                JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING, "", false),
                JsonOkonomiOpplysningUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_ANNET_BARN, "", false),
            )
        val oversiktUtgifter =
            listOf(
                JsonOkonomioversiktUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_BARNEHAGE, "", false),
                JsonOkonomioversiktUtgift(JsonKilde.BRUKER, SoknadJsonTyper.UTGIFTER_SFO, "", false),
            )
        val soknad = createSoknad(bekreftelser, opplysningUtgifter, oversiktUtgifter, true)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)

        val utgifterSporsmal = res.avsnitt[0].sporsmal
        assertThat(utgifterSporsmal).hasSize(3)
        assertThat(utgifterSporsmal[0].erUtfylt).isFalse // boutgifter
        assertThat(utgifterSporsmal[1].erUtfylt).isTrue // barneutgifter
        assertThat(utgifterSporsmal[2].erUtfylt).isTrue

        val barneutgifterFelter = utgifterSporsmal[2].felt
        assertThat(barneutgifterFelter).hasSize(5)
        validateFeltMedSvar(
            barneutgifterFelter!![0],
            Type.CHECKBOX,
            SvarType.LOCALE_TEKST,
            "utgifter.barn.true.utgifter.barnFritidsaktiviteter",
        )
        validateFeltMedSvar(barneutgifterFelter[1], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnehage")
        validateFeltMedSvar(barneutgifterFelter[2], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.sfo")
        validateFeltMedSvar(barneutgifterFelter[3], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnTannregulering")
        validateFeltMedSvar(barneutgifterFelter[4], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.annenBarneutgift")
    }

    private fun createSoknad(
        bekreftelser: List<JsonOkonomibekreftelse>,
        opplysningUtgifter: List<JsonOkonomiOpplysningUtgift>?,
        oversiktUtgifter: List<JsonOkonomioversiktUtgift>?,
        harForsorgerplikt: Boolean = false,
    ): JsonInternalSoknad {
        val forsorgerplikt = if (harForsorgerplikt) createForsorgerplikt() else JsonForsorgerplikt()
        val base = no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad()
        val soknad = requireNotNull(base.soknad)
        return base.copy(soknad = soknad.copy(data = soknad.data.copy(okonomi = JsonOkonomi(JsonOkonomiopplysninger(emptyList(), bekreftelser, null, opplysningUtgifter.orEmpty(), null), JsonOkonomioversikt(emptyList(), oversiktUtgifter.orEmpty(), emptyList())), familie = JsonFamilie(forsorgerplikt))))
    }

    private fun createForsorgerplikt() = JsonForsorgerplikt(JsonHarForsorgerplikt(JsonKilde.SYSTEM, true), null, listOf(JsonAnsvar(JsonBarn(JsonKilde.SYSTEM, JsonNavn("Grønn", "", "Jakke"), "2020-02-02", "11111111111", false), null, JsonErFolkeregistrertSammen(no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem.SYSTEM, true), null)))
}

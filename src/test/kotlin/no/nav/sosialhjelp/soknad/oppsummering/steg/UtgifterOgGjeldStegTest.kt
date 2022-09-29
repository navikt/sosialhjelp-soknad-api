package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
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
import java.lang.Boolean

internal class UtgifterOgGjeldStegTest {

    private val steg = UtgifterOgGjeldSteg()

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
        val bekreftelser = listOf(
            JsonOkonomibekreftelse()
                .withType(SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER)
                .withVerdi(Boolean.FALSE)
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
        val bekreftelser = listOf(
            JsonOkonomibekreftelse()
                .withType(SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER)
                .withVerdi(Boolean.TRUE)
        )
        val opplysningUtgifter = listOf(
            JsonOkonomiOpplysningUtgift().withType(SoknadJsonTyper.UTGIFTER_STROM),
            JsonOkonomiOpplysningUtgift().withType(SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT),
            JsonOkonomiOpplysningUtgift().withType(SoknadJsonTyper.UTGIFTER_OPPVARMING),
            JsonOkonomiOpplysningUtgift().withType(SoknadJsonTyper.UTGIFTER_ANNET_BO)
        )
        val oversiktUtgifter = listOf(
            JsonOkonomioversiktUtgift().withType(SoknadJsonTyper.UTGIFTER_HUSLEIE),
            JsonOkonomioversiktUtgift().withType(SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG)
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
        val bekreftelser = listOf(
            JsonOkonomibekreftelse()
                .withType(SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER)
                .withVerdi(Boolean.FALSE)
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
        val bekreftelser = listOf(
            JsonOkonomibekreftelse()
                .withType(SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER)
                .withVerdi(Boolean.TRUE)
        )
        val opplysningUtgifter = listOf(
            JsonOkonomiOpplysningUtgift().withType(SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER),
            JsonOkonomiOpplysningUtgift().withType(SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING),
            JsonOkonomiOpplysningUtgift().withType(SoknadJsonTyper.UTGIFTER_ANNET_BARN)
        )
        val oversiktUtgifter = listOf(
            JsonOkonomioversiktUtgift().withType(SoknadJsonTyper.UTGIFTER_BARNEHAGE),
            JsonOkonomioversiktUtgift().withType(SoknadJsonTyper.UTGIFTER_SFO)
        )
        val soknad = createSoknad(bekreftelser, opplysningUtgifter, oversiktUtgifter)
        setForsorgerplikt(soknad.soknad.data.familie)

        val res = steg.get(soknad)
        assertThat(res.avsnitt).hasSize(1)

        val utgifterSporsmal = res.avsnitt[0].sporsmal
        assertThat(utgifterSporsmal).hasSize(3)
        assertThat(utgifterSporsmal[0].erUtfylt).isFalse // boutgifter
        assertThat(utgifterSporsmal[1].erUtfylt).isTrue // barneutgifter
        assertThat(utgifterSporsmal[2].erUtfylt).isTrue

        val barneutgifterFelter = utgifterSporsmal[2].felt
        assertThat(barneutgifterFelter).hasSize(5)
        validateFeltMedSvar(barneutgifterFelter!![0], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnFritidsaktiviteter")
        validateFeltMedSvar(barneutgifterFelter[1], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnehage")
        validateFeltMedSvar(barneutgifterFelter[2], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.sfo")
        validateFeltMedSvar(barneutgifterFelter[3], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnTannregulering")
        validateFeltMedSvar(barneutgifterFelter[4], Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.annenBarneutgift")
    }

    private fun createSoknad(
        bekreftelser: List<JsonOkonomibekreftelse>,
        opplysningUtgifter: List<JsonOkonomiOpplysningUtgift>?,
        oversiktUtgifter: List<JsonOkonomioversiktUtgift>?
    ): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withSoknad(
                JsonSoknad()
                    .withData(
                        JsonData()
                            .withOkonomi(
                                JsonOkonomi()
                                    .withOpplysninger(
                                        JsonOkonomiopplysninger()
                                            .withUtgift(opplysningUtgifter)
                                            .withBekreftelse(bekreftelser)
                                    )
                                    .withOversikt(
                                        JsonOkonomioversikt()
                                            .withUtgift(oversiktUtgifter)
                                    )
                            )
                            .withFamilie(
                                JsonFamilie()
                                    .withForsorgerplikt(JsonForsorgerplikt())
                            )
                    )
            )
    }

    private fun setForsorgerplikt(familie: JsonFamilie) {
        familie.forsorgerplikt = JsonForsorgerplikt()
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(Boolean.TRUE)
            )
            .withAnsvar(
                listOf(
                    JsonAnsvar()
                        .withBarn(
                            JsonBarn()
                                .withKilde(JsonKilde.SYSTEM)
                                .withNavn(JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                .withFodselsdato("2020-02-02")
                                .withPersonIdentifikator("11111111111")
                        )
                        .withErFolkeregistrertSammen(JsonErFolkeregistrertSammen().withVerdi(Boolean.TRUE))
                        .withHarDeltBosted(null)
                )
            )
            .withBarnebidrag(null)
    }
}

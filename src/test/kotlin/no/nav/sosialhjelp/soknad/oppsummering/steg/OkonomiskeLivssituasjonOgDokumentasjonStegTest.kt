package no.nav.sosialhjelp.soknad.oppsummering.steg

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.oppsummering.dto.SvarType
import no.nav.sosialhjelp.soknad.oppsummering.dto.Type
import no.nav.sosialhjelp.soknad.oppsummering.steg.OkonomiskeOpplysningerOgVedleggSteg.OppsummeringVedleggInfo
import no.nav.sosialhjelp.soknad.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class OkonomiskeLivssituasjonOgDokumentasjonStegTest {
    private val okonomiskeOpplysningerOgVedleggSteg = OkonomiskeOpplysningerOgVedleggSteg

    @Test
    fun ingenting() {
        val soknad = createSoknad()

        val steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad, emptyList())
        assertThat(steg.avsnitt).hasSize(3)
        assertThat(steg.avsnitt[0].tittel).isEqualTo("inntektbolk.tittel")
        assertThat(steg.avsnitt[0].sporsmal).isEmpty()
        assertThat(steg.avsnitt[1].tittel).isEqualTo("utgifterbolk.tittel")
        assertThat(steg.avsnitt[1].sporsmal).isEmpty()
        assertThat(steg.avsnitt[2].tittel).isEqualTo("vedlegg.oppsummering.tittel")
        assertThat(steg.avsnitt[2].sporsmal).isEmpty()
    }

    @Test
    fun inntekter() {
        val base = createSoknad()
        val soknad =
            base.withOkonomi(
                base.soknad!!.data.okonomi.copy(
                    oversikt =
                        base.soknad!!.data.okonomi.oversikt!!.copy(
                            inntekt =
                                listOf(
                                    createInntekt(SoknadJsonTyper.JOBB, 42).copy(brutto = 142),
                                    createInntekt(SoknadJsonTyper.STUDIELAN, 111),
                                    createInntekt(SoknadJsonTyper.BARNEBIDRAG, null),
                                ),
                        ),
                ),
            )

        val steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad, emptyList())
        assertThat(steg.avsnitt).hasSize(3)
        assertThat(steg.avsnitt[0].tittel).isEqualTo("inntektbolk.tittel")
        assertThat(steg.avsnitt[0].sporsmal).hasSize(4)
        assertThat(steg.avsnitt[0].sporsmal[0].erUtfylt).isTrue
        assertThat(steg.avsnitt[0].sporsmal[1].erUtfylt).isTrue
        assertThat(steg.avsnitt[0].sporsmal[2].erUtfylt).isTrue
        assertThat(steg.avsnitt[0].sporsmal[3].erUtfylt).isFalse
    }

    @Test
    fun formuer() {
        val base = createSoknad()
        val soknad =
            base.withOkonomi(
                base.soknad!!.data.okonomi.copy(
                    oversikt =
                        base.soknad!!.data.okonomi.oversikt!!.copy(
                            formue =
                                listOf(
                                    createFormue(SoknadJsonTyper.FORMUE_VERDIPAPIRER, 42),
                                    createFormue(SoknadJsonTyper.FORMUE_BSU, 111),
                                    createFormue(SoknadJsonTyper.FORMUE_LIVSFORSIKRING, null),
                                ),
                        ),
                ),
            )

        val steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad, emptyList())
        assertThat(steg.avsnitt).hasSize(3)
        assertThat(steg.avsnitt[0].tittel).isEqualTo("inntektbolk.tittel")
        assertThat(steg.avsnitt[0].sporsmal).hasSize(3)
        assertThat(steg.avsnitt[0].sporsmal[0].erUtfylt).isTrue
        assertThat(steg.avsnitt[0].sporsmal[1].erUtfylt).isTrue
        assertThat(steg.avsnitt[0].sporsmal[2].erUtfylt).isFalse
    }

    @Test
    fun utbetalinger() {
        val base = createSoknad()
        val soknad =
            base.withOkonomi(
                base.soknad!!.data.okonomi.copy(
                    opplysninger =
                        base.soknad!!.data.okonomi.opplysninger.copy(
                            utbetaling =
                                listOf(
                                    createUtbetaling(SoknadJsonTyper.UTBETALING_NAVYTELSE, 42), // skal filtreres vekk
                                    createUtbetaling(SoknadJsonTyper.SLUTTOPPGJOER, 111),
                                    createUtbetaling(SoknadJsonTyper.UTBETALING_FORSIKRING, null),
                                    createUtbetaling(SoknadJsonTyper.UTBETALING_ANNET, null),
                                ),
                        ),
                ),
            )

        val steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad, emptyList())
        assertThat(steg.avsnitt).hasSize(3)
        assertThat(steg.avsnitt[0].tittel).isEqualTo("inntektbolk.tittel")
        assertThat(steg.avsnitt[0].sporsmal).hasSize(3)
        assertThat(steg.avsnitt[0].sporsmal[0].erUtfylt).isTrue
        assertThat(steg.avsnitt[0].sporsmal[0].tittel).isEqualTo("json.okonomi.opplysninger.arbeid.avsluttet")
        assertThat(steg.avsnitt[0].sporsmal[1].erUtfylt).isFalse
        assertThat(steg.avsnitt[0].sporsmal[1].tittel).isEqualTo("json.okonomi.opplysninger.inntekt.inntekter.forsikringsutbetalinger")
        assertThat(steg.avsnitt[0].sporsmal[2].erUtfylt).isFalse
        assertThat(steg.avsnitt[0].sporsmal[2].tittel).isEqualTo("json.okonomi.opplysninger.inntekt.inntekter.annet")
    }

    @Test
    fun utgifter() {
        val base = createSoknad()
        val soknad =
            base.withOkonomi(
                base.soknad!!.data.okonomi.copy(
                    opplysninger =
                        base.soknad!!.data.okonomi.opplysninger.copy(
                            utgift =
                                listOf(
                                    createOpplysningUtgift(SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER, 42), // skal filtreres vekk
                                    createOpplysningUtgift(SoknadJsonTyper.UTGIFTER_STROM, 111),
                                    createOpplysningUtgift(SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER, null),
                                ),
                        ),
                    oversikt =
                        base.soknad!!.data.okonomi.oversikt!!.copy(
                            utgift =
                                listOf(
                                    createOversiktUtgift(SoknadJsonTyper.UTGIFTER_BARNEHAGE, 42), // skal filtreres vekk
                                    createOversiktUtgift(SoknadJsonTyper.UTGIFTER_HUSLEIE, 111),
                                    createOversiktUtgift(SoknadJsonTyper.BARNEBIDRAG, 111),
                                    createOversiktUtgift(SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG, null),
                                ),
                        ),
                ),
            )

        val steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad, emptyList())
        assertThat(steg.avsnitt).hasSize(3)

        val utgifterAvsnitt = steg.avsnitt[1]
        assertThat(utgifterAvsnitt.tittel).isEqualTo("utgifterbolk.tittel")
        assertThat(utgifterAvsnitt.sporsmal).hasSize(7)
        assertThat(utgifterAvsnitt.sporsmal[0].erUtfylt).isTrue
        assertThat(utgifterAvsnitt.sporsmal[1].erUtfylt).isTrue
        assertThat(utgifterAvsnitt.sporsmal[2].erUtfylt).isFalse
        assertThat(utgifterAvsnitt.sporsmal[3].erUtfylt).isTrue
        assertThat(utgifterAvsnitt.sporsmal[4].erUtfylt).isTrue
        assertThat(utgifterAvsnitt.sporsmal[5].erUtfylt).isTrue
        assertThat(utgifterAvsnitt.sporsmal[6].erUtfylt).isFalse
    }

    @Test
    fun vedlegg() {
        val base = createSoknad()
        val filnavn = "fil.jpg"
        val soknad =
            base.copy(
                vedlegg =
                    base.vedlegg!!.copy(
                        vedlegg =
                            listOf(
                                createVedlegg("faktura", "oppvarming", "VedleggAlleredeSendt", null),
                                createVedlegg("kontooversikt", "sparekonto", "VedleggKreves", null),
                                createVedlegg("lonnslipp", "arbeid", "LastetOpp", listOf(JsonFiler(filnavn))),
                            ),
                    ),
            )
        val opplastedeVedlegg =
            listOf(
                OppsummeringVedleggInfo(
                    filnavn = filnavn,
                    id = "uuid-goes-here",
                ),
            )

        val steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad, opplastedeVedlegg)
        assertThat(steg.avsnitt).hasSize(3)

        val vedleggAvsnitt = steg.avsnitt[2]
        assertThat(vedleggAvsnitt.tittel).isEqualTo("vedlegg.oppsummering.tittel")
        assertThat(vedleggAvsnitt.sporsmal).hasSize(3)

        val vedlegg1 = vedleggAvsnitt.sporsmal[0]
        assertThat(vedlegg1.tittel).isEqualTo("vedlegg.faktura.oppvarming.tittel")
        assertThat(vedlegg1.erUtfylt).isTrue
        assertThat(vedlegg1.felt).hasSize(1)
        validateFeltMedSvar(vedlegg1.felt!![0], Type.TEKST, SvarType.LOCALE_TEKST, "opplysninger.vedlegg.alleredelastetopp")

        val vedlegg2 = vedleggAvsnitt.sporsmal[1]
        assertThat(vedlegg2.tittel).isEqualTo("vedlegg.kontooversikt.sparekonto.tittel")
        assertThat(vedlegg2.erUtfylt).isTrue
        assertThat(vedlegg2.felt).hasSize(1)
        validateFeltMedSvar(vedlegg2.felt!![0], Type.TEKST, SvarType.LOCALE_TEKST, "vedlegg.oppsummering.ikkelastetopp")

        val vedlegg3 = vedleggAvsnitt.sporsmal[2]
        assertThat(vedlegg3.tittel).isEqualTo("vedlegg.lonnslipp.arbeid.tittel")
        assertThat(vedlegg3.erUtfylt).isTrue
        assertThat(vedlegg3.felt).hasSize(1)
        assertThat(vedlegg3.felt!![0].type).isEqualTo(Type.VEDLEGG)
        assertThat(vedlegg3.felt[0].vedlegg).hasSize(1)
        assertThat(vedlegg3.felt[0].vedlegg!![0].filnavn).isEqualTo(filnavn)
        assertThat(vedlegg3.felt[0].vedlegg!![0].uuid).isEqualTo("uuid-goes-here")
    }

    private fun createInntekt(
        type: String,
        netto: Int?,
    ): JsonOkonomioversiktInntekt {
        return JsonOkonomioversiktInntekt(no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER, type, "", false, netto = netto)
    }

    private fun createFormue(
        type: String,
        belop: Int?,
    ): JsonOkonomioversiktFormue {
        return JsonOkonomioversiktFormue(no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER, type, "", false, belop = belop)
    }

    private fun createUtbetaling(
        type: String,
        belop: Int?,
    ): JsonOkonomiOpplysningUtbetaling {
        return JsonOkonomiOpplysningUtbetaling(no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER, type, "", false, belop = belop)
    }

    private fun createOpplysningUtgift(
        type: String,
        belop: Int?,
    ): JsonOkonomiOpplysningUtgift {
        return JsonOkonomiOpplysningUtgift(no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER, type, "", false, belop = belop)
    }

    private fun createOversiktUtgift(
        type: String,
        belop: Int?,
    ): JsonOkonomioversiktUtgift {
        return JsonOkonomioversiktUtgift(no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER, type, "", false, belop = belop)
    }

    private fun createVedlegg(
        type: String,
        tilleggsinfo: String,
        status: String,
        filer: List<JsonFiler>?,
    ): JsonVedlegg {
        return JsonVedlegg(type, tilleggsinfo, status, filer = filer.orEmpty())
    }

    private fun createSoknad(): JsonInternalSoknad {
        val base = no.nav.sosialhjelp.soknad.v2.createValidEmptyJsonInternalSoknad()
        val soknad = requireNotNull(base.soknad)
        return base.copy(soknad = soknad.copy(data = soknad.data.copy(okonomi = JsonOkonomi(JsonOkonomiopplysninger(emptyList(), emptyList(), null, emptyList(), null), JsonOkonomioversikt(emptyList(), emptyList(), emptyList())))), vedlegg = JsonVedleggSpesifikasjon(emptyList()))
    }

    private fun JsonInternalSoknad.withOkonomi(okonomi: JsonOkonomi): JsonInternalSoknad {
        val soknad = requireNotNull(soknad)
        return copy(soknad = soknad.copy(data = soknad.data.copy(okonomi = okonomi)))
    }
}

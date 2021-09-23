package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BARNEBIDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BSU;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_LIVSFORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_VERDIPAPIRER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM;
import static org.assertj.core.api.Assertions.assertThat;


class OkonomiskeOpplysningerOgVedleggStegTest {

    private final OkonomiskeOpplysningerOgVedleggSteg okonomiskeOpplysningerOgVedleggSteg = new OkonomiskeOpplysningerOgVedleggSteg();

    @Test
    void ingenting() {
        var soknad = createSoknad();

        var steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad);

        assertThat(steg.getAvsnitt()).hasSize(3);
        assertThat(steg.getAvsnitt().get(0).getTittel()).isEqualTo("inntektbolk.tittel");
        assertThat(steg.getAvsnitt().get(0).getSporsmal()).isEmpty();
        assertThat(steg.getAvsnitt().get(1).getTittel()).isEqualTo("utgifterbolk.tittel");
        assertThat(steg.getAvsnitt().get(1).getSporsmal()).isEmpty();
        assertThat(steg.getAvsnitt().get(2).getTittel()).isEqualTo("vedlegg.oppsummering.tittel");
        assertThat(steg.getAvsnitt().get(2).getSporsmal()).isEmpty();
    }

    @Test
    void inntekter() {
        var soknad = createSoknad();
        soknad.getSoknad().getData().getOkonomi().getOversikt()
                .setInntekt(List.of(
                        createInntekt(JOBB, 42).withBrutto(142),
                        createInntekt(STUDIELAN, 111),
                        createInntekt(BARNEBIDRAG, null)
                ));

        var steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad);

        assertThat(steg.getAvsnitt()).hasSize(3);
        assertThat(steg.getAvsnitt().get(0).getTittel()).isEqualTo("inntektbolk.tittel");
        assertThat(steg.getAvsnitt().get(0).getSporsmal()).hasSize(4);
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(1).getErUtfylt()).isTrue();
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(2).getErUtfylt()).isTrue();
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(3).getErUtfylt()).isFalse();
    }

    @Test
    void formuer() {
        var soknad = createSoknad();
        soknad.getSoknad().getData().getOkonomi().getOversikt()
                .setFormue(List.of(
                        createFormue(FORMUE_VERDIPAPIRER, 42),
                        createFormue(FORMUE_BSU, 111),
                        createFormue(FORMUE_LIVSFORSIKRING, null)
                ));

        var steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad);

        assertThat(steg.getAvsnitt()).hasSize(3);
        assertThat(steg.getAvsnitt().get(0).getTittel()).isEqualTo("inntektbolk.tittel");
        assertThat(steg.getAvsnitt().get(0).getSporsmal()).hasSize(3);
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(1).getErUtfylt()).isTrue();
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(2).getErUtfylt()).isFalse();
    }

    @Test
    void utbetalinger() {
        var soknad = createSoknad();
        soknad.getSoknad().getData().getOkonomi().getOpplysninger()
                .setUtbetaling(List.of(
                        createUtbetaling(UTBETALING_NAVYTELSE, 42), // skal filtreres vekk
                        createUtbetaling(SLUTTOPPGJOER, 111),
                        createUtbetaling(UTBETALING_FORSIKRING, null)
                ));

        var steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad);

        assertThat(steg.getAvsnitt()).hasSize(3);
        assertThat(steg.getAvsnitt().get(0).getTittel()).isEqualTo("inntektbolk.tittel");
        assertThat(steg.getAvsnitt().get(0).getSporsmal()).hasSize(2);
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(steg.getAvsnitt().get(0).getSporsmal().get(1).getErUtfylt()).isFalse();
    }

    @Test
    void utgifter() {
        var soknad = createSoknad();
        soknad.getSoknad().getData().getOkonomi().getOpplysninger()
                .setUtgift(List.of(
                        createOpplysningUtgift(UTGIFTER_BARN_FRITIDSAKTIVITETER, 42), // skal filtreres vekk
                        createOpplysningUtgift(UTGIFTER_STROM, 111),
                        createOpplysningUtgift(UTGIFTER_ANDRE_UTGIFTER, null)
                ));

        soknad.getSoknad().getData().getOkonomi().getOversikt()
                .setUtgift(List.of(
                        createOversiktUtgift(UTGIFTER_BARNEHAGE, 42), // skal filtreres vekk
                        createOversiktUtgift(UTGIFTER_HUSLEIE, 111),
                        createOversiktUtgift(BARNEBIDRAG, 111),
                        createOversiktUtgift(UTGIFTER_BOLIGLAN_AVDRAG, null)
                ));

        var steg = okonomiskeOpplysningerOgVedleggSteg.get(soknad);

        assertThat(steg.getAvsnitt()).hasSize(3);
        var utgifterAvsnitt = steg.getAvsnitt().get(1);
        assertThat(utgifterAvsnitt.getTittel()).isEqualTo("utgifterbolk.tittel");
        assertThat(utgifterAvsnitt.getSporsmal()).hasSize(7);
        assertThat(utgifterAvsnitt.getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(utgifterAvsnitt.getSporsmal().get(1).getErUtfylt()).isTrue();
        assertThat(utgifterAvsnitt.getSporsmal().get(2).getErUtfylt()).isFalse();
        assertThat(utgifterAvsnitt.getSporsmal().get(3).getErUtfylt()).isTrue();
        assertThat(utgifterAvsnitt.getSporsmal().get(4).getErUtfylt()).isTrue();
        assertThat(utgifterAvsnitt.getSporsmal().get(5).getErUtfylt()).isTrue();
        assertThat(utgifterAvsnitt.getSporsmal().get(6).getErUtfylt()).isFalse();
    }

    private JsonOkonomioversiktInntekt createInntekt(String type, Integer netto) {
        return new JsonOkonomioversiktInntekt()
                .withType(type)
                .withNetto(netto);
    }

    private JsonOkonomioversiktFormue createFormue(String type, Integer belop) {
        return new JsonOkonomioversiktFormue()
                .withType(type)
                .withBelop(belop);
    }

    private JsonOkonomiOpplysningUtbetaling createUtbetaling(String type, Integer belop) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withType(type)
                .withBelop(belop);
    }

    private JsonOkonomiOpplysningUtgift createOpplysningUtgift(String type, Integer belop) {
        return new JsonOkonomiOpplysningUtgift()
                .withType(type)
                .withBelop(belop);
    }

    private JsonOkonomioversiktUtgift createOversiktUtgift(String type, Integer belop) {
        return new JsonOkonomioversiktUtgift()
                .withType(type)
                .withBelop(belop);
    }


    public JsonInternalSoknad createSoknad() {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withData(new JsonData()
                                .withOkonomi(new JsonOkonomi()
                                        .withOversikt(new JsonOkonomioversikt())
                                        .withOpplysninger(new JsonOkonomiopplysninger())
                                )
                        )
                )
                .withVedlegg(new JsonVedleggSpesifikasjon());
    }
}
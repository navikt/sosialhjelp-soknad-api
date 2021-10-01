package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_UTBETALING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE;
import static org.assertj.core.api.Assertions.assertThat;

class AndreInntekterTest {

    private final AndreInntekter andreInntekter = new AndreInntekter();

    @Test
    void harIkkeUtfyltSporsmal() {
        var opplysninger = new JsonOkonomiopplysninger()
                .withBekreftelse(Collections.emptyList());

        var avsnitt = andreInntekter.getAvsnitt(opplysninger);
        assertThat(avsnitt.getSporsmal()).hasSize(1);
        var harAndreInntekterSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAndreInntekterSporsmal.getTittel()).isEqualTo("inntekt.inntekter.sporsmal");
        assertThat(harAndreInntekterSporsmal.getErUtfylt()).isFalse();
        assertThat(harAndreInntekterSporsmal.getFelt()).isNull();
    }

    @Test
    void harIkkeAndreInntekter() {
        var opplysninger = createOpplysninger(false);

        var avsnitt = andreInntekter.getAvsnitt(opplysninger);
        assertThat(avsnitt.getSporsmal()).hasSize(1);

        var harAndreInntekterSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAndreInntekterSporsmal.getTittel()).isEqualTo("inntekt.inntekter.sporsmal");
        assertThat(harAndreInntekterSporsmal.getErUtfylt()).isTrue();
        assertThat(harAndreInntekterSporsmal.getFelt()).hasSize(1);

        var harAndreInntekterSvar = harAndreInntekterSporsmal.getFelt().get(0);
        assertThat(harAndreInntekterSvar.getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harAndreInntekterSvar.getSvar().getValue()).isEqualTo("inntekt.inntekter.false");
        assertThat(harAndreInntekterSvar.getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
    }

    @Test
    void harAndreInntekterMenIkkeUtfyltHvaEierDu() {
        var opplysninger = createOpplysninger(true);
        opplysninger.setUtbetaling(Collections.emptyList());

        var avsnitt = andreInntekter.getAvsnitt(opplysninger);
        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harAndreInntekterSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAndreInntekterSporsmal.getTittel()).isEqualTo("inntekt.inntekter.sporsmal");
        assertThat(harAndreInntekterSporsmal.getErUtfylt()).isTrue();
        assertThat(harAndreInntekterSporsmal.getFelt()).hasSize(1);

        var harAndreInntekterSvar = harAndreInntekterSporsmal.getFelt().get(0);
        assertThat(harAndreInntekterSvar.getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harAndreInntekterSvar.getSvar().getValue()).isEqualTo("inntekt.inntekter.true");
        assertThat(harAndreInntekterSvar.getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var hvaHarDuMottattSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(hvaHarDuMottattSporsmal.getTittel()).isEqualTo("inntekt.inntekter.true.type.sporsmal");
        assertThat(hvaHarDuMottattSporsmal.getErUtfylt()).isFalse();
        assertThat(hvaHarDuMottattSporsmal.getFelt()).isNull();
    }

    @Test
    void harAndreInntekterMedUtbetalinger() {
        var opplysninger = createOpplysninger(true);
        opplysninger.setUtbetaling(List.of(
                createUtbetaling(UTBETALING_UTBYTTE),
                createUtbetaling(UTBETALING_SALG)
        ));

        var avsnitt = andreInntekter.getAvsnitt(opplysninger);
        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harAndreInntekterSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAndreInntekterSporsmal.getTittel()).isEqualTo("inntekt.inntekter.sporsmal");
        assertThat(harAndreInntekterSporsmal.getErUtfylt()).isTrue();
        assertThat(harAndreInntekterSporsmal.getFelt()).hasSize(1);

        var harAndreInntekterSvar = harAndreInntekterSporsmal.getFelt().get(0);
        assertThat(harAndreInntekterSvar.getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harAndreInntekterSvar.getSvar().getValue()).isEqualTo("inntekt.inntekter.true");
        assertThat(harAndreInntekterSvar.getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);

        var hvaHarDuMottattSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(hvaHarDuMottattSporsmal.getTittel()).isEqualTo("inntekt.inntekter.true.type.sporsmal");
        assertThat(hvaHarDuMottattSporsmal.getErUtfylt()).isTrue();
        assertThat(hvaHarDuMottattSporsmal.getFelt()).hasSize(2);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("json.okonomi.opplysninger.inntekt.inntekter.utbytte");
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(1).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(1).getSvar().getValue()).isEqualTo("json.okonomi.opplysninger.inntekt.inntekter.salg");
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(1).getSvar().getType()).isEqualTo(SvarType.LOCALE_TEKST);
    }

    @Test
    void harAndreInntekterUtenBeskrivelseAvAnnet() {
        var opplysninger = createOpplysninger(true);
        opplysninger.setUtbetaling(List.of(
                createUtbetaling(UTBETALING_ANNET)
        ));

        var avsnitt = andreInntekter.getAvsnitt(opplysninger);
        assertThat(avsnitt.getSporsmal()).hasSize(3);

        var harAndreInntekterSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAndreInntekterSporsmal.getTittel()).isEqualTo("inntekt.inntekter.sporsmal");
        assertThat(harAndreInntekterSporsmal.getErUtfylt()).isTrue();
        assertThat(harAndreInntekterSporsmal.getFelt()).hasSize(1);

        var harAndreInntekterSvar = harAndreInntekterSporsmal.getFelt().get(0);
        assertThat(harAndreInntekterSvar.getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harAndreInntekterSvar.getSvar().getValue()).isEqualTo("inntekt.inntekter.true");

        var hvaHarDuMottattSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(hvaHarDuMottattSporsmal.getTittel()).isEqualTo("inntekt.inntekter.true.type.sporsmal");
        assertThat(hvaHarDuMottattSporsmal.getErUtfylt()).isTrue();
        assertThat(hvaHarDuMottattSporsmal.getFelt()).hasSize(1);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("json.okonomi.opplysninger.inntekt.inntekter.annet");

        var annetBeskrivelseSporsmal = avsnitt.getSporsmal().get(2);
        assertThat(annetBeskrivelseSporsmal.getTittel()).isEqualTo("inntekt.inntekter.true.type.annet");
        assertThat(annetBeskrivelseSporsmal.getErUtfylt()).isFalse();
        assertThat(annetBeskrivelseSporsmal.getFelt()).isNull();
    }

    @Test
    void harAndreInntekterMedBeskrivelseAvAnnet() {
        var opplysninger = createOpplysninger(true);
        opplysninger.setUtbetaling(
                List.of(
                        createUtbetaling(UTBETALING_ANNET)
                )
        );
        opplysninger.setBeskrivelseAvAnnet(
                new JsonOkonomibeskrivelserAvAnnet()
                        .withUtbetaling("ANNEN")
        );

        var avsnitt = andreInntekter.getAvsnitt(opplysninger);
        assertThat(avsnitt.getSporsmal()).hasSize(3);

        var harAndreInntekterSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAndreInntekterSporsmal.getTittel()).isEqualTo("inntekt.inntekter.sporsmal");
        assertThat(harAndreInntekterSporsmal.getErUtfylt()).isTrue();
        assertThat(harAndreInntekterSporsmal.getFelt()).hasSize(1);

        var harAndreInntekterSvar = harAndreInntekterSporsmal.getFelt().get(0);
        assertThat(harAndreInntekterSvar.getType()).isEqualTo(Type.CHECKBOX);
        assertThat(harAndreInntekterSvar.getSvar().getValue()).isEqualTo("inntekt.inntekter.true");

        var hvaHarDuMottattSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(hvaHarDuMottattSporsmal.getTittel()).isEqualTo("inntekt.inntekter.true.type.sporsmal");
        assertThat(hvaHarDuMottattSporsmal.getErUtfylt()).isTrue();
        assertThat(hvaHarDuMottattSporsmal.getFelt()).hasSize(1);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(hvaHarDuMottattSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("json.okonomi.opplysninger.inntekt.inntekter.annet");

        var annetBeskrivelseSporsmal = avsnitt.getSporsmal().get(2);
        assertThat(annetBeskrivelseSporsmal.getTittel()).isEqualTo("inntekt.inntekter.true.type.annet");
        assertThat(annetBeskrivelseSporsmal.getErUtfylt()).isTrue();
        assertThat(annetBeskrivelseSporsmal.getFelt()).hasSize(1);
        assertThat(annetBeskrivelseSporsmal.getFelt().get(0).getType()).isEqualTo(Type.TEKST);
        assertThat(annetBeskrivelseSporsmal.getFelt().get(0).getSvar().getValue()).isEqualTo("ANNEN");
    }

    private JsonOkonomiopplysninger createOpplysninger(boolean harBekreftelse) {
        return new JsonOkonomiopplysninger()
                .withBekreftelse(Collections.singletonList(
                        new JsonOkonomibekreftelse()
                                .withType(BEKREFTELSE_UTBETALING)
                                .withVerdi(harBekreftelse)
                ));
    }

    private JsonOkonomiOpplysningUtbetaling createUtbetaling(String type) {
        return new JsonOkonomiOpplysningUtbetaling()
                .withType(type)
                .withKilde(JsonKilde.BRUKER);
    }

}

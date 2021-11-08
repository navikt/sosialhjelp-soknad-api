package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_VERDI;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class AnnenFormueTest {

    private final AnnenFormue annenFormue = new AnnenFormue();

    @Test
    void ikkeUtfylt() {
        var okonomi = new JsonOkonomi()
                .withOpplysninger(new JsonOkonomiopplysninger()
                        .withBekreftelse(Collections.emptyList()));

        var avsnitt = annenFormue.getAvsnitt(okonomi);
        assertThat(avsnitt.getSporsmal()).hasSize(1);
        var harAnnenFormueSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAnnenFormueSporsmal.getErUtfylt()).isFalse();
        assertThat(harAnnenFormueSporsmal.getFelt()).isNull();
    }

    @Test
    void harIkkeAnnenFormue() {
        var okonomi = createOkonomi(false);

        var avsnitt = annenFormue.getAvsnitt(okonomi);
        assertThat(avsnitt.getSporsmal()).hasSize(1);

        var harAnnenFormueSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAnnenFormueSporsmal.getErUtfylt()).isTrue();
        assertThat(harAnnenFormueSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(harAnnenFormueSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.false");
    }

    @Test
    void harAnnenFormueUtenHvaEierDuSvar() {
        var okonomi = createOkonomi(true);
        okonomi.withOversikt(new JsonOkonomioversikt()
                .withFormue(Collections.emptyList()));

        var avsnitt = annenFormue.getAvsnitt(okonomi);
        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var harAnnenFormueSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAnnenFormueSporsmal.getErUtfylt()).isTrue();
        assertThat(harAnnenFormueSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(harAnnenFormueSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true");

        var hvaEierDuSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(hvaEierDuSporsmal.getErUtfylt()).isFalse();
        assertThat(hvaEierDuSporsmal.getFelt()).isNull();
    }

    @Test
    void harAnnenFormueMedBeksrivelseAnnet() {
        var okonomi = createOkonomi(true);
        okonomi.withOversikt(new JsonOkonomioversikt()
                .withFormue(List.of(
                        createFormue(VERDI_BOLIG),
                        createFormue(VERDI_ANNET)
                )));
        okonomi.getOpplysninger()
                .setBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                        .withVerdi("verdi"));

        var avsnitt = annenFormue.getAvsnitt(okonomi);
        assertThat(avsnitt.getSporsmal()).hasSize(3);

        var harAnnenFormueSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(harAnnenFormueSporsmal.getErUtfylt()).isTrue();
        assertThat(harAnnenFormueSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(harAnnenFormueSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true");

        var hvaEierDuSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(hvaEierDuSporsmal.getErUtfylt()).isTrue();
        assertThat(hvaEierDuSporsmal.getFelt()).hasSize(2);
        validateFeltMedSvar(hvaEierDuSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true.type.bolig");
        validateFeltMedSvar(hvaEierDuSporsmal.getFelt().get(1), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.eierandeler.true.type.annet");

        var annetBeskrivelseSporsmal = avsnitt.getSporsmal().get(2);
        assertThat(annetBeskrivelseSporsmal.getErUtfylt()).isTrue();
        assertThat(annetBeskrivelseSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(annetBeskrivelseSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, "verdi");
    }

    private JsonOkonomi createOkonomi(boolean harBekreftelse) {
        return new JsonOkonomi()
                .withOpplysninger(
                        new JsonOkonomiopplysninger()
                                .withBekreftelse(List.of(
                                                new JsonOkonomibekreftelse()
                                                        .withType(BEKREFTELSE_VERDI)
                                                        .withVerdi(harBekreftelse)
                                        )
                                )
                );
    }

    private JsonOkonomioversiktFormue createFormue(String type) {
        return new JsonOkonomioversiktFormue()
                .withType(type)
                .withKilde(JsonKilde.BRUKER);
    }
}

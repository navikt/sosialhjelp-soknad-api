package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static org.assertj.core.api.Assertions.assertThat;

class StudielanTest {

    private final Studielan studielan = new Studielan();

    @Test
    void harIkkeUtfyltSporsmal() {
        var opplysninger = new JsonOkonomiopplysninger()
                .withBekreftelse(Collections.emptyList());

        var avsnitt = studielan.getAvsnitt(opplysninger);

        assertThat(avsnitt.getSporsmal()).hasSize(1);
        assertThat(avsnitt.getSporsmal().get(0).getTittel()).isEqualTo("inntekt.studielan.sporsmal");
        assertThat(avsnitt.getSporsmal().get(0).getErUtfylt()).isFalse();
        assertThat(avsnitt.getSporsmal().get(0).getFelt()).isNull();
    }

    @Test
    void harSvartJa() {
        var opplysninger = new JsonOkonomiopplysninger()
                .withBekreftelse(Collections.singletonList(
                        new JsonOkonomibekreftelse()
                                .withType(STUDIELAN)
                                .withVerdi(true)
                ));

        var avsnitt = studielan.getAvsnitt(opplysninger);

        assertThat(avsnitt.getSporsmal()).hasSize(1);
        assertThat(avsnitt.getSporsmal().get(0).getTittel()).isEqualTo("inntekt.studielan.sporsmal");
        assertThat(avsnitt.getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(avsnitt.getSporsmal().get(0).getFelt()).hasSize(1);
        assertThat(avsnitt.getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.CHECKBOX);
        assertThat(avsnitt.getSporsmal().get(0).getFelt().get(0).getSvar().getValue()).isEqualTo("inntekt.studielan.true");
        assertThat(avsnitt.getSporsmal().get(0).getFelt().get(0).getSvar().getType()).isEqualTo(SvarType.LOCALE);
    }
}

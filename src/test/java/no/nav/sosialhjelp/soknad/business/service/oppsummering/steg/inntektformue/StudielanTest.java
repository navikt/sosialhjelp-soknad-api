package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.inntektformue;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.STUDIELAN;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class StudielanTest {

    private final Studielan studielan = new Studielan();

    @Test
    void harIkkeUtfyltSporsmal() {
        var opplysninger = new JsonOkonomiopplysninger()
                .withBekreftelse(Collections.emptyList());

        var avsnitt = studielan.getAvsnitt(opplysninger);

        assertThat(avsnitt.getSporsmal()).hasSize(1);
        var studielanSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(studielanSporsmal.getTittel()).isEqualTo("inntekt.studielan.sporsmal");
        assertThat(studielanSporsmal.getErUtfylt()).isFalse();
        assertThat(studielanSporsmal.getFelt()).isNull();
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
        var studielanSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(studielanSporsmal.getTittel()).isEqualTo("inntekt.studielan.sporsmal");
        assertThat(studielanSporsmal.getErUtfylt()).isTrue();
        assertThat(studielanSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(studielanSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.studielan.true");
    }
}

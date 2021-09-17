package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class UtgifterOgGjeldStegTest {

    private final UtgifterOgGjeldSteg steg = new UtgifterOgGjeldSteg();

    @Test
    void boutgifterIkkeUtfylt() {
        var soknad = createSoknad(emptyList(), null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(2);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isFalse(); // boutgifter
        assertThat(utgifterSporsmal.get(1).getErUtfylt()).isFalse(); // barneutgifter
    }

    @Test
    void harIkkeBoutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType("boutgifter")
                        .withVerdi(Boolean.FALSE)
        );
        var soknad = createSoknad(bekreftelser, null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);
        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(2);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isTrue(); // boutgifter
        assertThat(utgifterSporsmal.get(1).getErUtfylt()).isFalse(); // barneutgifter
    }

    @Test
    void harBoutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType("boutgifter")
                        .withVerdi(Boolean.TRUE)
        );
        var opplysningUtgifter = List.of(
                new JsonOkonomiOpplysningUtgift().withType("strom"),
                new JsonOkonomiOpplysningUtgift().withType("kommunalAvgift"),
                new JsonOkonomiOpplysningUtgift().withType("oppvarming"),
                new JsonOkonomiOpplysningUtgift().withType("annenBoutgift")
        );
        var oversiktUtgifter = List.of(
                new JsonOkonomioversiktUtgift().withType("husleie"),
                new JsonOkonomioversiktUtgift().withType("boliglanAvdrag")
        );
        var soknad = createSoknad(bekreftelser, opplysningUtgifter, oversiktUtgifter);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(3);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isTrue(); // boutgifter
        assertThat(utgifterSporsmal.get(1).getErUtfylt()).isTrue();

        var boutgifterFelter = utgifterSporsmal.get(1).getFelt();
        assertThat(boutgifterFelter).hasSize(6);
        assertThat(boutgifterFelter.get(0).getSvar()).isEqualTo("utgifter.boutgift.true.type.husleie");
        assertThat(boutgifterFelter.get(1).getSvar()).isEqualTo("utgifter.boutgift.true.type.strom");
        assertThat(boutgifterFelter.get(2).getSvar()).isEqualTo("utgifter.boutgift.true.type.kommunalAvgift");
        assertThat(boutgifterFelter.get(3).getSvar()).isEqualTo("utgifter.boutgift.true.type.oppvarming");
        assertThat(boutgifterFelter.get(4).getSvar()).isEqualTo("utgifter.boutgift.true.type.boliglanAvdrag");
        assertThat(boutgifterFelter.get(5).getSvar()).isEqualTo("utgifter.boutgift.true.type.annenBoutgift");

        assertThat(utgifterSporsmal.get(2).getErUtfylt()).isFalse(); // barneutgifter
    }

    @Test
    void harIkkeBarneutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType("barneutgifter")
                        .withVerdi(Boolean.FALSE)
        );
        var soknad = createSoknad(bekreftelser, null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(2);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isFalse(); // boutgifter
        assertThat(utgifterSporsmal.get(1).getErUtfylt()).isTrue(); // barneutgifter
    }

    @Test
    void harBarneutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType("barneutgifter")
                        .withVerdi(Boolean.TRUE)
        );
        var opplysningUtgifter = List.of(
                new JsonOkonomiOpplysningUtgift().withType("barnFritidsaktiviteter"),
                new JsonOkonomiOpplysningUtgift().withType("barnTannregulering"),
                new JsonOkonomiOpplysningUtgift().withType("annenBarneutgift")
        );
        var oversiktUtgifter = List.of(
                new JsonOkonomioversiktUtgift().withType("barnehage"),
                new JsonOkonomioversiktUtgift().withType("sfo")
        );

        var soknad = createSoknad(bekreftelser, opplysningUtgifter, oversiktUtgifter);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(3);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isFalse(); // boutgifter
        assertThat(utgifterSporsmal.get(1).getErUtfylt()).isTrue(); // barneutgifter
        assertThat(utgifterSporsmal.get(2).getErUtfylt()).isTrue();

        var barneutgifterFelter = utgifterSporsmal.get(2).getFelt();
        assertThat(barneutgifterFelter).hasSize(5);
        assertThat(barneutgifterFelter.get(0).getSvar()).isEqualTo("utgifter.barn.true.utgifter.barnFritidsaktiviteter");
        assertThat(barneutgifterFelter.get(1).getSvar()).isEqualTo("utgifter.barn.true.utgifter.barnehage");
        assertThat(barneutgifterFelter.get(2).getSvar()).isEqualTo("utgifter.barn.true.utgifter.sfo");
        assertThat(barneutgifterFelter.get(3).getSvar()).isEqualTo("utgifter.barn.true.utgifter.barnTannregulering");
        assertThat(barneutgifterFelter.get(4).getSvar()).isEqualTo("utgifter.barn.true.utgifter.annenBarneutgift");
    }

    private JsonInternalSoknad createSoknad(List<JsonOkonomibekreftelse> bekreftelser, List<JsonOkonomiOpplysningUtgift> opplysningUtgifter, List<JsonOkonomioversiktUtgift> oversiktUtgifter) {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withData(new JsonData()
                                .withOkonomi(new JsonOkonomi()
                                        .withOpplysninger(new JsonOkonomiopplysninger()
                                                .withUtgift(opplysningUtgifter)
                                                .withBekreftelse(bekreftelser))
                                        .withOversikt(new JsonOkonomioversikt()
                                                .withUtgift(oversiktUtgifter))
                                )
                        )
                );
    }

}
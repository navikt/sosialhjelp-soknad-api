package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class UtgifterOgGjeldStegTest {

    private final UtgifterOgGjeldSteg steg = new UtgifterOgGjeldSteg();

    @Test
    void boutgifterIkkeUtfylt() {
        var soknad = createSoknad(emptyList(), null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(1);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isFalse(); // boutgifter
    }

    @Test
    void harIkkeBoutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType(BEKREFTELSE_BOUTGIFTER)
                        .withVerdi(Boolean.FALSE)
        );
        var soknad = createSoknad(bekreftelser, null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(1);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isTrue(); // boutgifter
    }

    @Test
    void harBoutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType(BEKREFTELSE_BOUTGIFTER)
                        .withVerdi(Boolean.TRUE)
        );
        var opplysningUtgifter = List.of(
                new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_STROM),
                new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_KOMMUNAL_AVGIFT),
                new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_OPPVARMING),
                new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_ANNET_BO)
        );
        var oversiktUtgifter = List.of(
                new JsonOkonomioversiktUtgift().withType(UTGIFTER_HUSLEIE),
                new JsonOkonomioversiktUtgift().withType(UTGIFTER_BOLIGLAN_AVDRAG)
        );

        var soknad = createSoknad(bekreftelser, opplysningUtgifter, oversiktUtgifter);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(2);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isTrue(); // boutgifter
        assertThat(utgifterSporsmal.get(1).getErUtfylt()).isTrue();

        var boutgifterFelter = utgifterSporsmal.get(1).getFelt();
        assertThat(boutgifterFelter).hasSize(6);
        validateFeltMedSvar(boutgifterFelter.get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.husleie");
        validateFeltMedSvar(boutgifterFelter.get(1), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.strom");
        validateFeltMedSvar(boutgifterFelter.get(2), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.kommunalAvgift");
        validateFeltMedSvar(boutgifterFelter.get(3), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.oppvarming");
        validateFeltMedSvar(boutgifterFelter.get(4), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.boliglanAvdrag");
        validateFeltMedSvar(boutgifterFelter.get(5), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.boutgift.true.type.annenBoutgift");
    }

    @Test
    void harIkkeBarneutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType(BEKREFTELSE_BARNEUTGIFTER)
                        .withVerdi(Boolean.FALSE)
        );
        var soknad = createSoknad(bekreftelser, null, null);

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(1);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isFalse(); // boutgifter
    }

    @Test
    void harBarneutgifter() {
        var bekreftelser = singletonList(
                new JsonOkonomibekreftelse()
                        .withType(BEKREFTELSE_BARNEUTGIFTER)
                        .withVerdi(Boolean.TRUE)
        );
        var opplysningUtgifter = List.of(
                new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_BARN_FRITIDSAKTIVITETER),
                new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_BARN_TANNREGULERING),
                new JsonOkonomiOpplysningUtgift().withType(UTGIFTER_ANNET_BARN)
        );
        var oversiktUtgifter = List.of(
                new JsonOkonomioversiktUtgift().withType(UTGIFTER_BARNEHAGE),
                new JsonOkonomioversiktUtgift().withType(UTGIFTER_SFO)
        );

        var soknad = createSoknad(bekreftelser, opplysningUtgifter, oversiktUtgifter);
        setForsorgerplikt(soknad.getSoknad().getData().getFamilie());

        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);

        var utgifterSporsmal = res.getAvsnitt().get(0).getSporsmal();
        assertThat(utgifterSporsmal).hasSize(3);
        assertThat(utgifterSporsmal.get(0).getErUtfylt()).isFalse(); // boutgifter
        assertThat(utgifterSporsmal.get(1).getErUtfylt()).isTrue(); // barneutgifter
        assertThat(utgifterSporsmal.get(2).getErUtfylt()).isTrue();

        var barneutgifterFelter = utgifterSporsmal.get(2).getFelt();
        assertThat(barneutgifterFelter).hasSize(5);
        validateFeltMedSvar(barneutgifterFelter.get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnFritidsaktiviteter");
        validateFeltMedSvar(barneutgifterFelter.get(1), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnehage");
        validateFeltMedSvar(barneutgifterFelter.get(2), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.sfo");
        validateFeltMedSvar(barneutgifterFelter.get(3), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.barnTannregulering");
        validateFeltMedSvar(barneutgifterFelter.get(4), Type.CHECKBOX, SvarType.LOCALE_TEKST, "utgifter.barn.true.utgifter.annenBarneutgift");
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
                                .withFamilie(new JsonFamilie()
                                        .withForsorgerplikt(new JsonForsorgerplikt()))
                        )
                );
    }

    private void setForsorgerplikt(JsonFamilie familie) {
        familie.setForsorgerplikt(
                new JsonForsorgerplikt()
                        .withHarForsorgerplikt(new JsonHarForsorgerplikt()
                                .withKilde(JsonKilde.SYSTEM)
                                .withVerdi(Boolean.TRUE))
                        .withAnsvar(singletonList(new JsonAnsvar()
                                .withBarn(new JsonBarn()
                                        .withKilde(JsonKilde.SYSTEM)
                                        .withNavn(new JsonNavn().withFornavn("Grønn").withEtternavn("Jakke"))
                                        .withFodselsdato("2020-02-02")
                                        .withPersonIdentifikator("11111111111"))
                                .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen().withVerdi(Boolean.TRUE))
                                .withHarDeltBosted(null)))
                        .withBarnebidrag(null)
        );
    }

}
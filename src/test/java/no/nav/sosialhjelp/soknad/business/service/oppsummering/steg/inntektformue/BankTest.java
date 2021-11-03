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

import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_SPARING;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_ANNET;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.FORMUE_BRUKSKONTO;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class BankTest {

    private final Bank bank = new Bank();

    @Test
    void ikkeUtfylt() {
        var okonomi = new JsonOkonomi()
                .withOpplysninger(new JsonOkonomiopplysninger());

        var avsnitt = bank.getAvsnitt(okonomi);

        assertThat(avsnitt.getSporsmal()).hasSize(1);

        var bankSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(bankSporsmal.getErUtfylt()).isTrue();
        assertThat(bankSporsmal.getFelt()).isNull();
    }

    @Test
    void valgtFlereBankFormuerMedBeskrivelseAvAnnet() {
        var okonomi = createOkonomi(true);
        okonomi.setOversikt(new JsonOkonomioversikt()
                .withFormue(List.of(
                        createFormue(FORMUE_BRUKSKONTO),
                        createFormue(FORMUE_ANNET)
                )));
        okonomi.getOpplysninger()
                .setBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                        .withSparing("sparing"));

        var avsnitt = bank.getAvsnitt(okonomi);

        assertThat(avsnitt.getSporsmal()).hasSize(2);

        var bankSporsmal = avsnitt.getSporsmal().get(0);
        assertThat(bankSporsmal.getErUtfylt()).isTrue();
        assertThat(bankSporsmal.getFelt()).hasSize(2);
        validateFeltMedSvar(bankSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bankinnskudd.true.type.brukskonto");
        validateFeltMedSvar(bankSporsmal.getFelt().get(1), Type.CHECKBOX, SvarType.LOCALE_TEKST, "inntekt.bankinnskudd.true.type.annet");

        var beskrivelseAnnetSporsmal = avsnitt.getSporsmal().get(1);
        assertThat(beskrivelseAnnetSporsmal.getErUtfylt()).isTrue();
        assertThat(beskrivelseAnnetSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(beskrivelseAnnetSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, "sparing");
    }

    private JsonOkonomi createOkonomi(boolean harBekreftelse) {
        return new JsonOkonomi()
                .withOpplysninger(
                        new JsonOkonomiopplysninger()
                                .withBekreftelse(List.of(
                                                new JsonOkonomibekreftelse()
                                                        .withType(BEKREFTELSE_SPARING)
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

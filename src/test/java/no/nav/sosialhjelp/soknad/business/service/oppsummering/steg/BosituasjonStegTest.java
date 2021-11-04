package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class BosituasjonStegTest {

    private final BosituasjonSteg steg = new BosituasjonSteg();

    @Test
    void ikkeUtfyltBotype_ikkeUtfyltAntallPersoner() {
        var soknad = createSoknad(null, null);
        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(2);

        var botypeSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(botypeSporsmal.getErUtfylt()).isFalse();
        assertThat(botypeSporsmal.getFelt()).isNull();

        var antallPersonerSporsmal = res.getAvsnitt().get(0).getSporsmal().get(1);
        assertThat(antallPersonerSporsmal.getErUtfylt()).isFalse();
        assertThat(antallPersonerSporsmal.getFelt()).isNull();
    }

    @Test
    void ikkeUtfyltBotype_utfyltAntallPersoner() {
        var soknad = createSoknad(null, 0);
        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(2);

        var botypeSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(botypeSporsmal.getErUtfylt()).isFalse();
        assertThat(botypeSporsmal.getFelt()).isNull();

        var antallPersonerSporsmal = res.getAvsnitt().get(0).getSporsmal().get(1);
        assertThat(antallPersonerSporsmal.getErUtfylt()).isTrue();
        assertThat(antallPersonerSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(antallPersonerSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, "0");
    }

    @Test
    void utfyltBotype_ikkeUtfyltAntallPersoner() {
        var soknad = createSoknad(Botype.ANNET, null);
        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(2);

        var botypeSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(botypeSporsmal.getErUtfylt()).isTrue();
        assertThat(botypeSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(botypeSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "bosituasjon.annet");

        var antallPersonerSporsmal = res.getAvsnitt().get(0).getSporsmal().get(1);
        assertThat(antallPersonerSporsmal.getErUtfylt()).isFalse();
        assertThat(antallPersonerSporsmal.getFelt()).isNull();
    }

    @Test
    void utfyltBotype_utfyltAntallPersoner() {
        var soknad = createSoknad(Botype.KRISESENTER, 11);
        var res = steg.get(soknad);

        assertThat(res.getAvsnitt()).hasSize(1);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(2);

        var botypeSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(botypeSporsmal.getErUtfylt()).isTrue();
        assertThat(botypeSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(botypeSporsmal.getFelt().get(0), Type.CHECKBOX, SvarType.LOCALE_TEKST, "bosituasjon.annet.botype.krisesenter");

        var antallPersonerSporsmal = res.getAvsnitt().get(0).getSporsmal().get(1);
        assertThat(antallPersonerSporsmal.getErUtfylt()).isTrue();
        assertThat(antallPersonerSporsmal.getFelt()).hasSize(1);
        validateFeltMedSvar(antallPersonerSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, "11");
    }

    private JsonInternalSoknad createSoknad(Botype botype, Integer antallPersoner) {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withData(new JsonData()
                                .withBosituasjon(new JsonBosituasjon()
                                        .withBotype(botype)
                                        .withAntallPersoner(antallPersoner))
                        )
                );
    }
}
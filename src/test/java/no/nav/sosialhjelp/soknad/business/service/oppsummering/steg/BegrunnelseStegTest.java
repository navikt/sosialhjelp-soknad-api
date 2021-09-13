package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BegrunnelseStegTest {

    private final BegrunnelseSteg steg = new BegrunnelseSteg();

    @Test
    void nullEmptyBegrunnelse() {
        var nullBegrunnelse = createSoknad(null, "");

        var res = this.steg.get(nullBegrunnelse);

        assertThat(res.getAvsnitt()).hasSize(1);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(2);
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getErUtfylt()).isFalse();
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt()).isNull();
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(1).getErUtfylt()).isFalse();
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(1).getFelt()).isNull();
    }

    @Test
    void utfyltBegrunnelse() {
        var nullBegrunnelse = createSoknad("hva jeg søker om", "hvorfor");

        var res = this.steg.get(nullBegrunnelse);

        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getErUtfylt()).isTrue();
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(0).getSvar()).isEqualTo("hva jeg søker om");
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(0).getFelt().get(0).getType()).isEqualTo(Type.TEKST);
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(1).getErUtfylt()).isTrue();
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(1).getFelt().get(0).getSvar()).isEqualTo("hvorfor");
        assertThat(res.getAvsnitt().get(0).getSporsmal().get(1).getFelt().get(0).getType()).isEqualTo(Type.TEKST);
    }

    private JsonInternalSoknad createSoknad(String hvaSokesOm, String hvorforSoke) {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withData(new JsonData()
                                .withBegrunnelse(new JsonBegrunnelse()
                                        .withHvaSokesOm(hvaSokesOm)
                                        .withHvorforSoke(hvorforSoke))
                        )
                );
    }

}
package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import org.junit.jupiter.api.Test;

import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.OppsummeringTestUtils.validateFeltMedSvar;
import static org.assertj.core.api.Assertions.assertThat;

class BegrunnelseStegTest {

    private final BegrunnelseSteg steg = new BegrunnelseSteg();

    @Test
    void nullEmptyBegrunnelse() {
        var soknadUtenBegrunnelse = createSoknad(null, "");

        var res = this.steg.get(soknadUtenBegrunnelse);

        assertThat(res.getAvsnitt()).hasSize(1);
        assertThat(res.getAvsnitt().get(0).getSporsmal()).hasSize(2);

        var hvaSokesOmSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(hvaSokesOmSporsmal.getErUtfylt()).isFalse();
        assertThat(hvaSokesOmSporsmal.getFelt()).isNull();

        var hvorforSokeSporsmal = res.getAvsnitt().get(0).getSporsmal().get(1);
        assertThat(hvorforSokeSporsmal.getErUtfylt()).isFalse();
        assertThat(hvorforSokeSporsmal.getFelt()).isNull();
    }

    @Test
    void utfyltBegrunnelse() {
        var soknadMedBegrunnelse = createSoknad("hva jeg søker om", "hvorfor");

        var res = this.steg.get(soknadMedBegrunnelse);

        var hvaSokesOmSporsmal = res.getAvsnitt().get(0).getSporsmal().get(0);
        assertThat(hvaSokesOmSporsmal.getErUtfylt()).isTrue();
        validateFeltMedSvar(hvaSokesOmSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, "hva jeg søker om");

        var hvorforSokeSporsmal = res.getAvsnitt().get(0).getSporsmal().get(1);
        assertThat(hvorforSokeSporsmal.getErUtfylt()).isTrue();
        validateFeltMedSvar(hvorforSokeSporsmal.getFelt().get(0), Type.TEKST, SvarType.TEKST, "hvorfor");
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
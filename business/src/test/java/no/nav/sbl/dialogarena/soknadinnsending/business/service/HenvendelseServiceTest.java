package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HenvendelseServiceTest {

    @Test
    public void lagBehandlingsId() {
        String behandlingsId = HenvendelseService.lagBehandlingsId(1, false);
        assertThat(behandlingsId).startsWith("11");
    }

    @Test
    public void lagBehandlingsIdForSelvstendigNaringsdrivende() {
        String behandlingsId = HenvendelseService.lagBehandlingsId(1, true);
        assertThat(behandlingsId).startsWith("33");
    }
}
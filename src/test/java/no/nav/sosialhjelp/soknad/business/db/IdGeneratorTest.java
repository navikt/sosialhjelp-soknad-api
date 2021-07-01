package no.nav.sosialhjelp.soknad.business.db;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdGeneratorTest {
    
    @Test
    public void skalGenererId() {
        String behandlingsId = IdGenerator.lagBehandlingsId(1l);
        assertThat(behandlingsId).isEqualTo("100000001");
    }
    
    @Test(expected=RuntimeException.class)
    public void skalFaaFeilVedForHoyId() {
        IdGenerator.lagBehandlingsId(10000000000000l);
    }
}

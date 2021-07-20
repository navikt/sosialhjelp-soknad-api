package no.nav.sosialhjelp.soknad.business.db;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class IdGeneratorTest {
    
    @Test
    void skalGenererId() {
        String behandlingsId = IdGenerator.lagBehandlingsId(1l);
        assertThat(behandlingsId).isEqualTo("100000001");
    }
    
    @Test
    void skalFaaFeilVedForHoyId() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> IdGenerator.lagBehandlingsId(10000000000000l));
    }
}

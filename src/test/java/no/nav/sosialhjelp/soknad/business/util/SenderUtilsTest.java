package no.nav.sosialhjelp.soknad.business.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static java.lang.System.setProperty;
import static no.nav.sosialhjelp.soknad.business.util.SenderUtils.createPrefixedBehandlingsIdInNonProd;
import static org.assertj.core.api.Assertions.assertThat;

class SenderUtilsTest {

    String originalBehandlingsId = "behandlingsId";

    @AfterEach
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    void createPrefixedBehandlingsId_inProd_shouldNotBePrefixed() {
        System.clearProperty("environment.name");
        String prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertThat(prefixedBehandlingsId).isEqualTo(originalBehandlingsId);

        setProperty("environment.name", "p");
        prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertThat(prefixedBehandlingsId).isEqualTo(originalBehandlingsId);

        setProperty("environment.name", "ukjent");
        prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertThat(prefixedBehandlingsId).isEqualTo(originalBehandlingsId);
    }

    @Test
    void createPrefixedBehandlingsId_inNonProd_shouldBePrefixedWithEnvironmentName() {
        setProperty("environment.name", "q0");
        String prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertThat(prefixedBehandlingsId).isEqualTo("q0-" + originalBehandlingsId);

        setProperty("environment.name", "q1");
        prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertThat(prefixedBehandlingsId).isEqualTo("q1-" + originalBehandlingsId);
    }
}

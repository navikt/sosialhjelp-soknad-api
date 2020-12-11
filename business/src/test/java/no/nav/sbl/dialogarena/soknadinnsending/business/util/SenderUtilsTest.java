package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import org.junit.After;
import org.junit.Test;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.SenderUtils.createPrefixedBehandlingsIdInNonProd;
import static org.junit.Assert.*;

public class SenderUtilsTest {

    String originalBehandlingsId = "behandlingsId";

    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void createPrefixedBehandlingsId_inProd_shouldNotBePrefixed() {
        System.clearProperty("environment.name");
        String prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertEquals(originalBehandlingsId, prefixedBehandlingsId);

        setProperty("environment.name", "p");
        prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertEquals(originalBehandlingsId, prefixedBehandlingsId);
    }

    @Test
    public void createPrefixedBehandlingsId_inNonProd_shouldBePrefixedWithEnvironmentName() {
        setProperty("environment.name", "q0");
        String prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertEquals("q0-" + originalBehandlingsId, prefixedBehandlingsId);

        setProperty("environment.name", "q1");
        prefixedBehandlingsId = createPrefixedBehandlingsIdInNonProd(originalBehandlingsId);
        assertEquals("q1-" + originalBehandlingsId, prefixedBehandlingsId);
    }
}

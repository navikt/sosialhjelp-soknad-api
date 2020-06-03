package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServiceUtilsTest {

    @Test
    public void skalStrippeVekkFnutter() {
        String utenFnutter = ServiceUtils.stripVekkFnutter("\"123\"");
        assertEquals("123", utenFnutter);
    }
}
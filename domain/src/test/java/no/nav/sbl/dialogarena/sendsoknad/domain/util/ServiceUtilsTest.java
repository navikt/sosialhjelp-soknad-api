package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import org.junit.After;
import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.feilmeldingUtenFnr;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.isNonProduction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ServiceUtilsTest {


    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void skalStrippeVekkFnutter() {
        String utenFnutter = ServiceUtils.stripVekkFnutter("\"123\"");
        assertEquals("123", utenFnutter);
    }

    @Test
    public void skalFjerne_alleFnr_fraFeilmelding() {
        String str = "12121212121 feilmelding som har flere fnr 12345678911 og 11111111111";

        String res = feilmeldingUtenFnr(str);

        assertEquals("[FNR] feilmelding som har flere fnr [FNR] og [FNR]", res);
    }

    @Test
    public void skalFjerne_fnr_fraUrl() {
        String str = "/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/11111111111/oppgave/inntekt?fnr=12121212121&a=b";

        String res = feilmeldingUtenFnr(str);

        assertEquals("/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/[FNR]/oppgave/inntekt?fnr=[FNR]&a=b", res);
    }

    @Test
    public void skalIkkeFjerne_12siffretTall_fraFeilmelding() {
        String str = "feilmelding som har for langt fnr 111112222233";

        String res = feilmeldingUtenFnr(str);

        assertEquals(str, res);
    }

    @Test
    public void skalIkkeFjerne_10siffretTall_fraFeilmelding() {
        String str = "feilmelding som har for kort fnr 1111122222";

        String res = feilmeldingUtenFnr(str);

        assertEquals(str, res);
    }

    @Test
    public void skalFjerne_11siffretTallWrappetMedHermetegn_fraFeilmelding() {
        String str = "feilmelding som har fnr \"12345612345\"";

        String res = feilmeldingUtenFnr(str);

        assertEquals("feilmelding som har fnr \"[FNR]\"", res);
    }

    @Test
    public void skalIkkeFeile_medNull_iFeilmelding() {
        String res = feilmeldingUtenFnr(null);

        assertNull(res);
    }

    @Test
    public void isNonProduction_skalGiTrue_forNonProd() {
        System.setProperty("environment.name", "q0");
        assertTrue(isNonProduction());
        System.setProperty("environment.name", "q1");
        assertTrue(isNonProduction());
        System.setProperty("environment.name", "labs-gcp");
        assertTrue(isNonProduction());
        System.setProperty("environment.name", "dev-gcp");
        assertTrue(isNonProduction());
        System.setProperty("environment.name", "local");
        assertTrue(isNonProduction());
        System.setProperty("environment.name", "test");
        assertTrue(isNonProduction());
    }

    @Test
    public void isNonProduction_skalGiFalse_forProd() {
        System.setProperty("environment.name", "p");
        assertFalse(isNonProduction());
        System.setProperty("environment.name", "prod");
        assertFalse(isNonProduction());
        System.setProperty("environment.name", "prod-sbs");
        assertFalse(isNonProduction());
    }

    @Test
    public void isNonProduction_skalGiFalse_forUkjentMiljo() {
        System.clearProperty("environment.name");
        assertFalse(isNonProduction());
        System.setProperty("environment.name", "");
        assertFalse(isNonProduction());
        System.setProperty("environment.name", "ukjent");
        assertFalse(isNonProduction());
        System.setProperty("environment.name", "mock");
        assertFalse(isNonProduction());
    }
}
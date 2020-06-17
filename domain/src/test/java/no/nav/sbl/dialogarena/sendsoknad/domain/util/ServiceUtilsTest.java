package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.feilmeldingUtenFnr;
import static org.junit.Assert.assertEquals;

public class ServiceUtilsTest {

    @Test
    public void skalStrippeVekkFnutter() {
        String utenFnutter = ServiceUtils.stripVekkFnutter("\"123\"");
        assertEquals("123", utenFnutter);
    }

    @Test
    public void skalFjerne_alleFnr_fraFeilmelding() {
        String str = "12121212121 feilmelding som har flere fnr 12345678911 og 11111111111";
        String[] split = str.split("\\b");

        String res = feilmeldingUtenFnr(str);

        assertEquals("[FNR] feilmelding som har flere fnr [FNR] og [FNR]", res);
    }

    @Test
    public void skalFjerne_fnr_fraUrl() {
        String str = "/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/11111111111/oppgave/inntekt?fnr=12121212121&a=b";
        String[] split = str.split("\\b");

        String res = feilmeldingUtenFnr(str);

        assertEquals("/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/[FNR]/oppgave/inntekt?fnr=[FNR]&a=b", res);
    }

    @Test
    public void skalIkkeFjerne_12siffretTall_fraFeilmelding() {
        String forLangtFnr = "111112222233";
        String str = "feilmelding som har fnr " + forLangtFnr;

        String res = feilmeldingUtenFnr(str);

        assertEquals(str, res);
    }

    @Test
    public void skalIkkeFjerne_10siffretTall_fraFeilmelding() {
        String forKortFnr = "1111122222";
        String str = "feilmelding som har fnr " + forKortFnr;

        String res = feilmeldingUtenFnr(str);

        assertEquals(str, res);
    }

    @Test
    public void skalFjerne_11siffretTallWrappetMedHermetegn_fraFeilmelding() {
        String fnr = "\"12345612345\"";
        String str = "feilmelding som har fnr " + fnr;

        String res = feilmeldingUtenFnr(str);

        assertEquals("feilmelding som har fnr \"[FNR]\"", res);
    }
}
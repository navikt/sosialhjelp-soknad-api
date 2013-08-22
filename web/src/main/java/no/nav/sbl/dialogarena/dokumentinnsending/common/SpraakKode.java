package no.nav.sbl.dialogarena.dokumentinnsending.common;


import no.nav.sbl.dialogarena.soknad.kodeverk.Spraak;

import org.apache.wicket.util.cookies.CookieUtils;

public class SpraakKode {
    private static final String SPRAAK = "dokumentinnsending_spraak";
    public static final String DEFAULT_LANGUAGE_CODE = "53";


    private static CookieUtils cookieUtils = new CookieUtils();

    public static Spraak gjeldendeSpraak() {
        return Spraak.fromEnonic(cookieUtils.load(SPRAAK));
    }

    public static void setGjeldendeSpraak(String kode) {
        cookieUtils.save(SPRAAK, kode);
    }
}

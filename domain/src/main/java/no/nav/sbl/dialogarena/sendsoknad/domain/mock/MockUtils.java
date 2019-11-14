package no.nav.sbl.dialogarena.sendsoknad.domain.mock;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.isRunningInProd;

public class MockUtils {

    private final static String IS_ALLTID_SEND_TIL_NAV_TESTKOMMUNE = "digisosapi.sending.alltidTilTestkommune.enable";
    private final static String IS_ALLTID_HENT_KOMMUNEINFO_FRA_NAV_TESTKOMMUNE = "digisosapi.henting.alltidTestkommune.enable";

    public static boolean isTillatMockRessurs() {
        return Boolean.parseBoolean(System.getProperty("tillatMockRessurs", "false"));
    }

    public static boolean isAlltidSendTilNavTestkommune(){
        return Boolean.valueOf(System.getProperty(IS_ALLTID_SEND_TIL_NAV_TESTKOMMUNE, "false")) && !isRunningInProd();
    }

    public static boolean isAlltidHentKommuneInfoFraNavTestkommune(){
        return Boolean.valueOf(System.getProperty(IS_ALLTID_HENT_KOMMUNEINFO_FRA_NAV_TESTKOMMUNE, "false")) && !isRunningInProd();
    }
}

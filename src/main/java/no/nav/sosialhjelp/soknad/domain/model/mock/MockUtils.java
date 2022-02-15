package no.nav.sosialhjelp.soknad.domain.model.mock;

public final class MockUtils {

    private MockUtils() {
    }

    private static final String IS_ALLTID_SEND_TIL_NAV_TESTKOMMUNE = "digisosapi.sending.alltidTilTestkommune.enable";
    private static final String IS_ALLTID_HENT_KOMMUNEINFO_FRA_NAV_TESTKOMMUNE = "digisosapi.henting.alltidTestkommune.enable";

    public static boolean isMockAltProfil() {
        return Boolean.parseBoolean(System.getProperty("mockAltProfil", "false"));
    }

    public static boolean isRunningWithInMemoryDb() {
        return Boolean.parseBoolean(System.getProperty("no.nav.sosialhjelp.soknad.hsqldb", "false"));
    }

    public static boolean isAlltidSendTilNavTestkommune(){
        return Boolean.parseBoolean(System.getProperty(IS_ALLTID_SEND_TIL_NAV_TESTKOMMUNE, "false"));
    }

    public static boolean isAlltidHentKommuneInfoFraNavTestkommune(){
        return Boolean.parseBoolean(System.getProperty(IS_ALLTID_HENT_KOMMUNEINFO_FRA_NAV_TESTKOMMUNE, "false"));
    }
}

package no.nav.sbl.dialogarena.sendsoknad.domain.mock;

public class MockUtils {
    public static boolean isTillatMockRessurs() {
        return Boolean.parseBoolean(System.getProperty("tillatMockRessurs", "false"));
    }
}

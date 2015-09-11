package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import static java.lang.Boolean.valueOf;
import static java.lang.System.getProperty;

public class MockUtil {

    public static final String TILLATMOCK_PROPERTY = "tillatmock";
    public static final String TILLATSTARTDATOMOCK_PROPERTY = "tillatstartdatomock";
    public static final String VALGTMAANED_PROPERTY = "valgtmaaned";
    public static final String ALLOW_MOCK = "true";
    public static final String DEFAULT_MOCK_TILLATT = "false";

    private static final String DEFAULT_VALGTMAANED = "0";

    public static boolean mockSetupErTillatt() {
        return valueOf(getProperty(TILLATMOCK_PROPERTY, DEFAULT_MOCK_TILLATT));
    }

    public static boolean startdatoMockErTillattOgAktivert() {
        return getProperty(TILLATSTARTDATOMOCK_PROPERTY, DEFAULT_MOCK_TILLATT).equalsIgnoreCase(ALLOW_MOCK);
    }

    public static Integer valgtMaaned() {
        return Integer.parseInt(getProperty(VALGTMAANED_PROPERTY, DEFAULT_VALGTMAANED)) + 1;
    }
}

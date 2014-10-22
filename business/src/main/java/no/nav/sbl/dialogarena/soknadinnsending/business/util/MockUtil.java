package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import static java.lang.System.getProperty;

public class MockUtil {

    public static final String TILLATMOCK_PROPERTY = "tillatmock";
    public static final String TILLATSTARTDATOMOCK_PROPERTY = "tillatstartdatomock";
    public static final String VALGTMAANED_PROPERTY = "valgtmaaned";
    private static final String DEFAULT_MOCK_TILATT = "false";
    private static final String DEFAULT_VALGTMAANED = "0";
    public static final String ALLOW_MOCK = "true";

    public static boolean mockErTillattOgAktivert() {
        return getProperty(TILLATMOCK_PROPERTY, DEFAULT_MOCK_TILATT).equalsIgnoreCase(ALLOW_MOCK);
    }

    public static boolean startdatoMockErTillattOgAktivert() {
        return getProperty(TILLATSTARTDATOMOCK_PROPERTY, DEFAULT_MOCK_TILATT).equalsIgnoreCase(ALLOW_MOCK);
    }

    public static Integer valgtMaaned() {
        return Integer.parseInt(getProperty(VALGTMAANED_PROPERTY, DEFAULT_VALGTMAANED)) + 1;
    }
}

package no.nav.sbl.dialogarena.dokumentinnsending;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {
    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = TestUtils.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }

}

package no.nav.sbl.dialogarena.modigcertificates;

/*
    Copied from https://github.com/navikt/modig-testcertificates-safe-fork
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.io.File.createTempFile;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.io.IOUtils.copy;

public final class FileUtils {
    private FileUtils() {
    }

    public static File putInTempFile(InputStream data) {
        return putInTempFile(data, "test");
    }

    public static File putInTempFile(InputStream data, String navnPrefix) {
        if (data == null) {
            throw new IllegalArgumentException("InputStream==null. Nothing to write to temporary file");
        }
        try {
            File tempFile = createTempFile(navnPrefix + currentTimeMillis(), ".tmp");
            tempFile.deleteOnExit();
            try (OutputStream out = new FileOutputStream(tempFile)) {
                copy(data, out);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
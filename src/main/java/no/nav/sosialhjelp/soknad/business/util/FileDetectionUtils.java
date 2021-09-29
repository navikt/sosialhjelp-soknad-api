package no.nav.sosialhjelp.soknad.business.util;

import org.apache.tika.Tika;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public final class FileDetectionUtils {

    private static final Logger log = getLogger(FileDetectionUtils.class);

    private FileDetectionUtils() {
    }

    public static String getMimeType(byte[] bytes) {
        return new Tika().detect(bytes);
    }

    public static TikaFileType detectTikaType(byte[] bytes) {
        String type = new Tika().detect(bytes);
        if (type.equalsIgnoreCase("application/pdf")) {
            return TikaFileType.PDF;
        }
        if (type.equalsIgnoreCase("text/x-matlab")) {
            log.info("Tika detekterte mimeType text/x-matlab. Vi antar at dette egentlig er en PDF, men som ikke har korrekte magic bytes (%PDF).");
            return TikaFileType.PDF;
        }
        if (type.equalsIgnoreCase("image/png")) {
            return TikaFileType.PNG;
        }
        if (type.equalsIgnoreCase("image/jpeg")) {
            return TikaFileType.JPEG;
        }

        return TikaFileType.UNKNOWN;
    }
}

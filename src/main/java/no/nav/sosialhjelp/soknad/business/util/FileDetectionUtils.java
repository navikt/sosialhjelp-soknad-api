package no.nav.sosialhjelp.soknad.business.util;

import org.apache.tika.Tika;
import org.slf4j.Logger;

import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.APPLICATION_PDF;
import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.IMAGE_JPEG;
import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.IMAGE_PNG;
import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.TEXT_X_MATLAB;
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
        if (type.equalsIgnoreCase(APPLICATION_PDF)) {
            return TikaFileType.PDF;
        }
        if (type.equalsIgnoreCase(TEXT_X_MATLAB)) {
            log.info("Tika detekterte mimeType text/x-matlab. Vi antar at dette egentlig er en PDF, men som ikke har korrekte magic bytes (%PDF).");
            return TikaFileType.PDF;
        }
        if (type.equalsIgnoreCase(IMAGE_PNG)) {
            return TikaFileType.PNG;
        }
        if (type.equalsIgnoreCase(IMAGE_JPEG)) {
            return TikaFileType.JPEG;
        }

        return TikaFileType.UNKNOWN;
    }

}

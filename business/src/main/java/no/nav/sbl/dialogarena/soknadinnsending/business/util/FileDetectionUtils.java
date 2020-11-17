package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import org.apache.tika.Tika;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class FileDetectionUtils {

    private static final Logger log = getLogger(FileDetectionUtils.class);

    public static String getMimeType(byte[] bytes) {
        return new Tika().detect(bytes);
    }

    public static boolean isImage(byte[] bytes) {
        String mimeType = new Tika().detect(bytes);
        log.info("FileDetectionUtils.isImage - mimeType={}", mimeType);
        return (mimeType.equalsIgnoreCase("image/png") || mimeType.equalsIgnoreCase("image/jpeg"));
    }

    public static boolean isPdf(byte[] bytes) {
        return new Tika().detect(bytes).equalsIgnoreCase("application/pdf");
    }
}

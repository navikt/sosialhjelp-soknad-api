package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import org.apache.tika.Tika;

public class FileDetectionUtils {

    public static String getMimeType(byte[] bytes) {
        return new Tika().detect(bytes);
    }

    public static boolean isImage(byte[] bytes) {
        String mimeType = new Tika().detect(bytes);
        return (mimeType.equalsIgnoreCase("image/png") || mimeType.equalsIgnoreCase("image/jpeg"));
    }

    public static boolean isPdf(byte[] bytes) {
        return new Tika().detect(bytes).equalsIgnoreCase("application/pdf");
    }
}

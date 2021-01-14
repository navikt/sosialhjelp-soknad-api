package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import org.apache.tika.Tika;

public class FileDetectionUtils {

    public static String getMimeType(byte[] bytes) {
        return new Tika().detect(bytes);
    }

    public static TikaFileType detectTikaType(byte[] bytes) {
        String type = new Tika().detect(bytes);
        if(type.equalsIgnoreCase("application/pdf") ) return TikaFileType.PDF;
        if(type.equalsIgnoreCase("image/png") ) return TikaFileType.PNG;
        if(type.equalsIgnoreCase("image/jpeg") ) return TikaFileType.JPEG;

        return TikaFileType.UNKNOWN;
    }
}

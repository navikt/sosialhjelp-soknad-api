package no.nav.sosialhjelp.soknad.common.filedetection

import no.nav.sosialhjelp.soknad.business.util.MimeTypes
import org.apache.tika.Tika
import org.slf4j.LoggerFactory

object FileDetectionUtils {

    private val log = LoggerFactory.getLogger(FileDetectionUtils::class.java)

    fun getMimeType(bytes: ByteArray?): String {
        return Tika().detect(bytes)
    }

    fun detectTikaType(bytes: ByteArray?): TikaFileType {
        val type = Tika().detect(bytes)
        if (type.equals(MimeTypes.APPLICATION_PDF, ignoreCase = true)) {
            return TikaFileType.PDF
        }
        if (type.equals(MimeTypes.TEXT_X_MATLAB, ignoreCase = true)) {
            log.info("Tika detekterte mimeType text/x-matlab. Vi antar at dette egentlig er en PDF, men som ikke har korrekte magic bytes (%PDF).")
            return TikaFileType.PDF
        }
        if (type.equals(MimeTypes.IMAGE_PNG, ignoreCase = true)) {
            return TikaFileType.PNG
        }
        return if (type.equals(MimeTypes.IMAGE_JPEG, ignoreCase = true)) {
            TikaFileType.JPEG
        } else TikaFileType.UNKNOWN
    }
}

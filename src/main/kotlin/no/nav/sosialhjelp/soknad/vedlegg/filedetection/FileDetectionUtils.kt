package no.nav.sosialhjelp.soknad.vedlegg.filedetection

import org.apache.tika.Tika
import org.slf4j.LoggerFactory

object FileDetectionUtils {

    private val log = LoggerFactory.getLogger(FileDetectionUtils::class.java)

    fun detectMimeType(bytes: ByteArray?): String {
        val mimeType = Tika().detect(bytes).lowercase()
        return if (mimeType == MimeTypes.TEXT_X_MATLAB) MimeTypes.APPLICATION_PDF else mimeType
    }

    fun mapToTikaType(mimeType: String): TikaFileType = when (mimeType) {
        MimeTypes.APPLICATION_PDF -> TikaFileType.PDF
        MimeTypes.TEXT_X_MATLAB -> {
            log.info("Tika detekterte mimeType text/x-matlab. Vi antar at dette egentlig er en PDF, men som ikke har korrekte magic bytes (%PDF).")
            TikaFileType.PDF
        }
        MimeTypes.IMAGE_PNG -> TikaFileType.PNG
        MimeTypes.IMAGE_JPEG -> TikaFileType.JPEG
        else -> TikaFileType.UNKNOWN
    }
}
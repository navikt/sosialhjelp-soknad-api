package no.nav.sosialhjelp.soknad.vedlegg.filedetection

import org.apache.tika.Tika

object FileDetectionUtils {
    fun detectMimeType(bytes: ByteArray?): String {
        val mimeType = Tika().detect(bytes).lowercase()
        return if (mimeType == MimeTypes.TEXT_X_MATLAB) MimeTypes.APPLICATION_PDF else mimeType
    }
}

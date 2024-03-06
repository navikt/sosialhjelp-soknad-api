package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.springframework.web.multipart.MultipartFile
import java.util.*

data class FileConversionUpload(val file: MultipartFile) {
    private val uuid = UUID.randomUUID()
    val extension = getExtension(validatedFilename(file.originalFilename))
    val unconvertedName = "$uuid.$extension"
    val convertedName = "$uuid.pdf"
    val mimeType = FileDetectionUtils.detectMimeType(file.bytes)

    val bytes: ByteArray
        get() = file.bytes

    override fun toString(): String = "FileConversionUpload(uuid='$uuid' mime='$mimeType', extension='$extension', size=${bytes.size}b)"

    private fun validatedFilename(filename: String?): String {
        requireNotNull(filename) { "Filnavn er null" }
        require(filename.isNotBlank()) { "Filnavn er tomt" }
        return filename
    }

    private fun getExtension(filename: String): String {
        val extension = filename.substringAfterLast(".", "")
        require(extension.isNotBlank()) { "Finner ikke filtype" }
        return extension
    }
}

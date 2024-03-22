package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.File

data class FileConversionUpload(val file: MultipartFile) {
    val unconvertedName = validatedFilename(file.originalFilename)
    private val splitFilename = splitFilename(unconvertedName)
    val extension = splitFilename.second
    val convertedName = "${splitFilename.first}.pdf"
    val mimeType = FileDetectionUtils.detectMimeType(file.bytes)

    val bytes: ByteArray
        get() = file.bytes

    override fun toString(): String = "FileConversionUpload(mime='$mimeType', extension='$extension', size=${bytes.size}b)"

    private fun validatedFilename(filename: String?): String {
        requireNotNull(filename) { "Filnavn er null" }
        require(filename.isNotBlank()) { "Filnavn er tomt" }
        return filename
    }

    init {
        require(this.file.size > 0) { "Fil [$this] for konvertering er tom." }
        if (this.file.contentType != this.mimeType) {
            log.warn("Ulik MIME mellom klientdata og Tika: ${this.file.contentType} != ${this.mimeType}")
        }
    }

    private fun splitFilename(filename: String): Pair<String, String> {
        val file = File(filename)
        require(file.extension.isNotBlank()) { "Finner ikke filtype" }
        return Pair(file.nameWithoutExtension, file.extension)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

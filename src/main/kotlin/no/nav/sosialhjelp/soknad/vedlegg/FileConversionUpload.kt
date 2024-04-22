package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FileConversionException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
        if (filename.isNullOrBlank()) {
            throw FileConversionException(HttpStatus.BAD_REQUEST, "Filnavn er tomt", "")
        }
        return filename
    }

    init {
        if (this.file.isEmpty) {
            throw FileConversionException(HttpStatus.BAD_REQUEST, "Fil for konvertering er tom.", "")
        }
        if (this.file.contentType != this.mimeType) {
            log.warn("Ulik MIME mellom klientdata og Tika: ${this.file.contentType} != ${this.mimeType}")
        }
    }

    private fun splitFilename(filename: String): Pair<String, String> {
        return File(filename).let {
            if (it.extension.isBlank()) {
                throw FileConversionException(HttpStatus.BAD_REQUEST, "Finner ikke filtype", "")
            }
            Pair(it.nameWithoutExtension, it.extension)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

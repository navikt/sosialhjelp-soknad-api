package no.nav.sosialhjelp.soknad.vedlegg

import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadError
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadFileEncrypted
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadUnsupportedMediaType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.TikaFileType
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException

object VedleggUtils {
    private val log by logger()

    fun validerFilOgReturnerNyttFilnavn(
        filnavn: String,
        bytes: ByteArray,
    ): String {
        val uuidRandom = UUID.randomUUID()

        val fileType = validerFil(bytes, filnavn)
        return lagFilnavn(filnavn, fileType, uuidRandom)
    }

    fun lagFilnavn(
        opplastetNavn: String,
        fileType: TikaFileType,
        uuid: UUID,
    ): String {
        var filnavn = opplastetNavn
        val fileExtension = findFileExtension(opplastetNavn)

        if (fileExtension != null) {
            val separatorPosition = opplastetNavn.lastIndexOf(".")
            if (separatorPosition != -1) {
                filnavn = opplastetNavn.substring(0, separatorPosition)
            }
        }
        try {
            filnavn = URLDecoder.decode(filnavn, StandardCharsets.UTF_8.toString())
        } catch (e: UnsupportedEncodingException) {
            log.warn("Klarte ikke å URIdecode fil med navn $filnavn", e)
        }

        filnavn =
            filnavn
                .replace("æ", "e")
                .replace("ø", "o")
                .replace("å", "a")
                .replace("Æ", "E")
                .replace("Ø", "O")
                .replace("Å", "A")

        filnavn = filnavn.replace("[^a-zA-Z0-9_-]".toRegex(), "")

        if (filnavn.length > 50) {
            filnavn = filnavn.substring(0, 50)
        }

        filnavn += "-" + uuid.toString().split("-").toTypedArray()[0]
        filnavn +=
            if (!fileExtension.isNullOrEmpty() && erTikaOgFileExtensionEnige(fileExtension, fileType)) {
                fileExtension
            } else {
                log.info("Opplastet vedlegg mangler fil extension -> setter fil extension lik validert filtype = ${fileType.extension}")
                fileType.extension
            }

        return filnavn
    }

    fun validerFil(
        data: ByteArray,
        filnavn: String,
    ): TikaFileType {
        val mimeType = FileDetectionUtils.detectMimeType(data)
        val fileType = FileDetectionUtils.mapToTikaType(mimeType)

        if (fileType == TikaFileType.UNKNOWN) {
            val filType = findFileExtension(filnavn)
            throw DokumentUploadUnsupportedMediaType(
                "Ugyldig filtype for opplasting. Mimetype var $mimeType, filtype var $filType",
            )
        }
        if (fileType == TikaFileType.JPEG || fileType == TikaFileType.PNG) {
            validerFiltypeForBilde(filnavn)
        }
        if (fileType == TikaFileType.PDF) {
            sjekkOmPdfErGyldig(data)
        }
        return fileType
    }

    private fun findFileExtension(filnavn: String): String? {
        val sisteIndexForPunktum = filnavn.lastIndexOf(".")
        if (sisteIndexForPunktum < 0) {
            return null
        }
        val fileExtension = filnavn.substring(sisteIndexForPunktum)
        return if (!isValidFileExtension(fileExtension)) {
            null
        } else {
            fileExtension
        }
    }

    private fun isValidFileExtension(fileExtension: String): Boolean {
        val validFileExtensions = listOf(".pdf", ".jpeg", ".jpg", ".png")
        return validFileExtensions.contains(fileExtension.lowercase(Locale.getDefault()))
    }

    private fun validerFiltypeForBilde(filnavn: String) {
        val fileExtension = findFileExtension(filnavn)
        if (fileExtension == null) {
            log.info("Opplastet bilde validerer OK, men mangler filtype for fil")
        }
        val lowercaseFilenavn = filnavn.lowercase(Locale.getDefault())
        if (lowercaseFilenavn.endsWith(".jfif") || lowercaseFilenavn.endsWith(".pjpeg") || lowercaseFilenavn.endsWith(".pjp")) {
            throw DokumentUploadUnsupportedMediaType(
                "Ugyldig filtype for opplasting. Filtype var $fileExtension",
            )
        }
    }

    private fun sjekkOmPdfErGyldig(data: ByteArray) {

        runCatching {
            Loader.loadPDF(data)
                .use { document ->
                    if (document.isDocEmpty()) log.warn("PDF er tom")
                    if (document.isEncrypted) throw DokumentUploadFileEncrypted()
                }
        }
            .getOrElse {
                when(it) {
                    is InvalidPasswordException -> throw DokumentUploadFileEncrypted()
                    is IOException -> throw DokumentUploadError("Kunne ikke lagre fil", it, "vedlegg.opplasting.feil.generell")
                    else -> throw it
                }
            }
    }

    private fun erTikaOgFileExtensionEnige(
        fileExtension: String,
        fileType: TikaFileType,
    ): Boolean {
        if (TikaFileType.JPEG == fileType) {
            return ".jpg".equals(fileExtension, ignoreCase = true) || ".jpeg".equals(fileExtension, ignoreCase = true)
        }
        if (TikaFileType.PNG == fileType) {
            return ".png".equals(fileExtension, ignoreCase = true)
        }
        return if (TikaFileType.PDF == fileType) {
            ".pdf".equals(fileExtension, ignoreCase = true)
        } else {
            false
        }
    }
}

private fun PDDocument.isDocEmpty(): Boolean = pages.count == 0 || pages.get(0).contentStreams?.hasNext() != true
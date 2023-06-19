package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.OpplastingException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.TikaFileType
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.text.PDFTextStripper
import org.bouncycastle.jcajce.provider.digest.SHA512
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

object VedleggUtils {

    private val log by logger()

    fun getSha512FromByteArray(bytes: ByteArray?): String {
        if (bytes == null) {
            return ""
        }
        val sha512 = SHA512.Digest()
        sha512.update(bytes)
        return Hex.toHexString(sha512.digest())
    }

    fun lagFilnavn(opplastetNavn: String, fileType: TikaFileType, uuid: UUID): String {
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

        filnavn = filnavn
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
        filnavn += if (!fileExtension.isNullOrEmpty() && erTikaOgFileExtensionEnige(fileExtension, fileType)) {
            fileExtension
        } else {
            log.info("Opplastet vedlegg mangler fil extension -> setter fil extension lik validert filtype = ${fileType.extension}")
            fileType.extension
        }

        return filnavn
    }

    fun validerFil(data: ByteArray, filnavn: String): TikaFileType {
        val mimeType = FileDetectionUtils.detectMimeType(data)
        val fileType = FileDetectionUtils.mapToTikaType(mimeType)

        if (fileType == TikaFileType.UNKNOWN) {
            val filType = findFileExtension(filnavn)
            throw UgyldigOpplastingTypeException(
                "Ugyldig filtype for opplasting. Mimetype var $mimeType, filtype var $filType",
                null,
                "opplasting.feilmelding.feiltype"
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

    fun finnVedleggEllerKastException(vedleggstype: String, soknadUnderArbeid: SoknadUnderArbeid): JsonVedlegg {
        return JsonVedleggUtils.getVedleggFromInternalSoknad(soknadUnderArbeid)
            .firstOrNull { vedleggstype == it.type + "|" + it.tilleggsinfo }
            ?: throw IkkeFunnetException("Dette vedlegget tilhører $vedleggstype utgift som har blitt tatt bort fra søknaden. Er det flere tabber oppe samtidig?")
    }

    private fun findFileExtension(filnavn: String): String? {
        val sisteIndexForPunktum = filnavn.lastIndexOf(".")
        if (sisteIndexForPunktum < 0) {
            return null
        }
        val fileExtension = filnavn.substring(sisteIndexForPunktum)
        return if (!isValidFileExtension(fileExtension)) {
            null
        } else fileExtension
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
            throw UgyldigOpplastingTypeException(
                "Ugyldig filtype for opplasting. Filtype var $fileExtension",
                null,
                "opplasting.feilmelding.feiltype"
            )
        }
    }

    private fun sjekkOmPdfErGyldig(data: ByteArray) {
        try {
            PDDocument.load(ByteArrayInputStream(data))
                .use { document ->
                    val text = PDFTextStripper().getText(document)
                    if (text == null || text.isEmpty()) {
                        log.warn("PDF er tom") // En PDF med ett helt blankt ark generert av word gir text = "\r\n"
                    }
                    if (document.isEncrypted) {
                        throw UgyldigOpplastingTypeException(
                            "PDF kan ikke være kryptert.",
                            null,
                            "opplasting.feilmelding.pdf.kryptert"
                        )
                    }
                }
        } catch (e: InvalidPasswordException) {
            throw UgyldigOpplastingTypeException(
                "PDF kan ikke være krypert.",
                null,
                "opplasting.feilmelding.pdf.kryptert"
            )
        } catch (e: IOException) {
            throw OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell")
        }
    }

    private fun erTikaOgFileExtensionEnige(fileExtension: String, fileType: TikaFileType): Boolean {
        if (TikaFileType.JPEG == fileType) {
            return ".jpg".equals(fileExtension, ignoreCase = true) || ".jpeg".equals(fileExtension, ignoreCase = true)
        }
        if (TikaFileType.PNG == fileType) {
            return ".png".equals(fileExtension, ignoreCase = true)
        }
        return if (TikaFileType.PDF == fileType) {
            ".pdf".equals(fileExtension, ignoreCase = true)
        } else false
    }
}

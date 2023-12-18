package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import java.io.ByteArrayInputStream
import java.io.File

@Component
class KonvertereTilPdfClient(
    @Value("\${konvertering_til_pdf}") private val baseUrl: String,
    webClientBuilder: WebClient.Builder
) {
    private val log by logger()
    companion object Routes {
        private const val LIBRE_OFFICE = "/forms/libreoffice/convert"
    }

    private val webClient = unproxiedWebClientBuilder(webClientBuilder)
        .baseUrl(baseUrl)
        .build()

    fun konvertereTilPdf(fil: MultipartFile): ByteArray {
        log.info("Starter konvertering av fil")



    }

    private class ByteArrayMultipartFile(
        private val filnavn: String,
        private val bytes: ByteArray
    ): MultipartFile {
        override fun getInputStream() = ByteArrayInputStream(bytes)
        override fun getName() = filnavn
        override fun getOriginalFilename() = filnavn
        override fun getContentType() = FileDetectionUtils.detectMimeType(bytes)
        override fun isEmpty(): Boolean {
            return bytes.isEmpty()
        }
        override fun getSize() = bytes.size.toLong()
        override fun getBytes() = bytes
        override fun transferTo(dest: File) {
            FileUtils.writeByteArrayToFile(dest, bytes)
        }
    }
}

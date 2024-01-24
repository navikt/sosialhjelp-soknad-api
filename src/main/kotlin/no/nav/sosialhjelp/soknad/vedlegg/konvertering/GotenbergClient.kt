package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.unproxiedWebClientBuilder
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.File

@Component
class GotenbergClient(
    @Value("\${fil-konvertering_url}") private val baseUrl: String,
    private val webClientBuilder: WebClient.Builder
) : FileConverter {

    companion object GotenbergConsts {
        private const val LIBRE_OFFICE_ROUTE = "/forms/libreoffice/convert"
        private const val GOTENBERG_TRACE_HEADER = "gotenberg-trace"
    }

    private var trace = "[NA]"
    private val webClient = buildWebClient()

    override fun toPdf(filename: String, bytes: ByteArray): ByteArray {
        val multipartBody = MultipartBodyBuilder().run {
            part("files", ByteArrayMultipartFile(filename, bytes).resource)
            build()
        }

        return convertFileRequest(filename, multipartBody)
    }

    private fun convertFileRequest(filename: String, multipartBody: MultiValueMap<String, HttpEntity<*>>): ByteArray {
        return webClient.post()
            .uri(baseUrl + LIBRE_OFFICE_ROUTE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .body(BodyInserters.fromMultipartData(multipartBody))
            .exchangeToMono { evaluateClientResponse(filename, it) }
            .block() ?: throw IllegalStateException("[$trace] Innhold i konvertert fil \"$filename\" er null.")
    }

    private fun evaluateClientResponse(filename: String, response: ClientResponse): Mono<ByteArray> {
        trace = response.headers().header(GOTENBERG_TRACE_HEADER).first()
        log.info("[$trace] Konverterer fil \"$filename\"")

        return if (response.statusCode().is2xxSuccessful) { response.bodyToMono(ByteArray::class.java) } else {
            response.bodyToMono(String::class.java)
                .flatMap { body -> Mono.error(FileConverterException(response.statusCode(), body, trace)) }
        }
    }
    private fun buildWebClient(): WebClient {
        return unproxiedWebClientBuilder(webClientBuilder)
            .baseUrl(baseUrl)
            .defaultHeaders {
                it.contentType = MediaType.MULTIPART_FORM_DATA
                it.accept = listOf(MediaType.APPLICATION_PDF, MediaType.TEXT_PLAIN)
            }.build()
    }

    private val log by logger()

    private class ByteArrayMultipartFile(
        private val filnavn: String,
        private val bytes: ByteArray
    ) : MultipartFile {
        override fun getInputStream() = ByteArrayInputStream(bytes)
        override fun getName() = "file"
        override fun getOriginalFilename() = filnavn
        override fun getContentType() = FileDetectionUtils.detectMimeType(bytes)
        override fun isEmpty(): Boolean = bytes.isEmpty()
        override fun getSize() = bytes.size.toLong()
        override fun getBytes() = bytes
        override fun transferTo(dest: File) { FileUtils.writeByteArrayToFile(dest, bytes) }
    }
}

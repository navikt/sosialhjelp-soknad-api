package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-interceptor", "no-redis", "test"])
class FileConverterIntegrationTest {
    private val endpoint: String = "/vedlegg/konverter"

    @Autowired
    private lateinit var webClient: WebTestClient

    @Autowired
    private lateinit var virusScanner: VirusScanner

    @Autowired
    private lateinit var fileConverter: FileConverter

    @BeforeEach
    fun setup() {
        every { virusScanner.scan(any(), any(), any(), any()) } just runs
    }

    @Test
    fun `Gyldig fil skal gi status 200 og content = PDF`() {
        every { fileConverter.toPdf(any(), any()) } returns ExampleFileRepository.EXCEL_FILE.readBytes()

        doPost(producer = BodyInserters.fromMultipartData(createMultipartBody()))
            .expectStatus().is2xxSuccessful
            .expectHeader().contentType("${MediaType.APPLICATION_PDF};charset=UTF-8")
            .expectBody().returnResult().responseBodyContent?.let {
                FileDetectionUtils.detectMimeType(it) == MediaType.APPLICATION_PDF_VALUE
            } ?: throw IllegalStateException("ByteArray er null")
    }

    @Test
    fun `Ikke stottet fil skal gi exception`() {
        every { fileConverter.toPdf(any(), any()) } throws FileConverterException(
            httpStatus = HttpStatus.BAD_REQUEST,
            msg = "Unknown format",
            trace = UUID.randomUUID().toString()
        )

        doPost(producer = BodyInserters.fromValue(createMultipartBody()))
            .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .expectHeader().contentType("${MediaType.APPLICATION_JSON};charset=UTF-8")
            .expectBody().jsonPath("id").isEqualTo("filkonvertering_error")
    }

    @Test
    fun `Ikke stottet content-type`() {
        doPost(
            contentType = MediaType.APPLICATION_JSON,
            BodyInserters.fromValue("{id: 44}")
        )
            .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .expectHeader().contentType("application/problem+json;charset=UTF-8")
            .expectBody()
            .jsonPath("detail").isEqualTo("Content-Type 'application/json' is not supported.")
    }

    fun doPost(
        contentType: MediaType = MediaType.MULTIPART_FORM_DATA,
        producer: BodyInserter<*, in ClientHttpRequest>
    ): WebTestClient.ResponseSpec {
        return webClient.post()
            .uri(endpoint)
            .header(HttpHeaders.CONTENT_TYPE, contentType.toString())
            .body(producer)
            .exchange()
    }

    fun createMultipartBody(): MultiValueMap<String, *> {
        return MockMultipartFile(
            "file",
            ExampleFileRepository.EXCEL_FILE.name,
            MediaType.MULTIPART_FORM_DATA.toString(),
            ExampleFileRepository.EXCEL_FILE.readBytes()
        ).let {
            val builder = MultipartBodyBuilder()
            builder.part("file", it.resource)
            builder.build()
        }
    }

    @TestConfiguration
    class FileConverterTestConfig {
        @Bean
        fun fileConverter() = mockk<FileConverter>()
    }
}

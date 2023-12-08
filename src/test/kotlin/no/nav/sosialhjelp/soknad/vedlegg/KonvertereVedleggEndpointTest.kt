package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE_OLD
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import java.io.File

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-redis", "test"])
class KonvertereVedleggEndpointTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val endpoint: String = "/vedlegg/konverter"

    @Test
    fun `Konvertere excel-fil til pdf via post-request og verifisere fil`() { doRequestAndVerifyExpectations(EXCEL_FILE) }
    @Test
    fun `Konvertere word-fil til pdf via post-request og verifisere fil`() { doRequestAndVerifyExpectations(WORD_FILE) }
    @Test
    fun `Konvertere csv-fil pdf via post-request og verifisere fil`() { doRequestAndVerifyExpectations(CSV_FILE) }

    @Test
    fun `Filtype som ikke er stottet skal feile`() {
        doPost(createMultipartBody(EXCEL_FILE_OLD))
            .expectStatus().is5xxServerError
            .expectHeader().valueEquals("Content-Type", "application/json;charset=UTF-8")
            .expectBody().jsonPath("id").isEqualTo("konvertering_til_pdf_error")
    }

    fun doRequestAndVerifyExpectations(file: File) {
        doPost(createMultipartBody(file))
            .expectStatus().isOk
            .expectHeader().valueEquals(
                HttpHeaders.CONTENT_TYPE,
                "${MimeTypes.APPLICATION_PDF};charset=UTF-8"
            )
            .expectHeader().valueEquals(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${file.nameWithoutExtension + ".pdf\""}"
            )
            .returnResult<ByteArray>()
            .responseBody.blockFirst()?.let {
                assertThat(detectMimeType(it)).isEqualTo(MimeTypes.APPLICATION_PDF)
            }
    }

    fun doPost(bodyBuilder: MultipartBodyBuilder) = webTestClient.post()
        .uri(endpoint)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .exchange()

    fun createMultipartBody(fil: File) = MultipartBodyBuilder()
        .apply {
            part("file", fil.readBytes())
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "form-data; name=file; filename=${fil.name}"
                )
        }
}

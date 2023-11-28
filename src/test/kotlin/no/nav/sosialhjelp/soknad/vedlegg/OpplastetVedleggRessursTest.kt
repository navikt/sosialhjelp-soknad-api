package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.TestApplication
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository
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

@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["no-redis", "test"])
class OpplastetVedleggRessursTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val behandlingsId: String = "1234567890"
    private val endpoint: String = "/opplastetVedlegg/{behandlingsId}/konverter"

    @Test
    fun `Konvertere fil via request`() {
        val excelFile = ExampleFileRepository.EXCEL_FILE
        val expectedFilnavn = excelFile.nameWithoutExtension + ".pdf"

        val result = doPost(createMultipartBody(excelFile))

        assertThat(detectMimeType(result.responseBody.blockFirst()) )
            .isEqualTo(MimeTypes.APPLICATION_PDF)
        assertThat(result.responseHeaders.contentDisposition.filename)
            .isEqualTo(expectedFilnavn)
    }

    @Test
    fun `Filtype som ikke er stottet skal feile`() {

        val excelFileOld = ExampleFileRepository.EXCEL_FILE_OLD
        val expectedFilnavn = excelFileOld.nameWithoutExtension + ".pdf"

        // TODO mappe exception til noe mer informativt
        val result = doPost(createMultipartBody(excelFileOld))

        val a = 4
    }

    fun createMultipartBody(fil: File) = MultipartBodyBuilder()
        .apply {
            part("file", fil.readBytes())
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "form-data; name=file; filename=${fil.name}"
                )
        }

    fun doPost(bodyBuilder: MultipartBodyBuilder) = webTestClient.post()
        .uri(endpoint, behandlingsId)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .exchange()
        .returnResult<ByteArray>()
}

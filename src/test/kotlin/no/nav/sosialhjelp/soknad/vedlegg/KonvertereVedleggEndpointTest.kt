package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE_OLD
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.multipart
import java.io.File
import java.net.URI

@WebMvcTest(value = [KonvertereVedleggController::class])
@ActiveProfiles(profiles = ["no-interceptor", "no-redis", "test"])
class KonvertereVedleggEndpointTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val endpoint: String = "/vedlegg/konverter"

    @Test
    fun `Konvertere excel-fil til pdf via post-request og verifisere fil`() { doRequestAndVerifyExpectations(EXCEL_FILE) }
    @Test
    fun `Konvertere word-fil til pdf via post-request og verifisere fil`() { doRequestAndVerifyExpectations(WORD_FILE) }
    @Test
    fun `Konvertere csv-fil pdf via post-request og verifisere fil`() { doRequestAndVerifyExpectations(CSV_FILE) }

    @Test
    fun `Filtype som ikke er stottet skal feile`() {
        doPost(EXCEL_FILE_OLD)
            .andExpect {
                status { is5xxServerError() }
                content { contentType(MimeTypes.APPLICATION_JSON) }
                jsonPath("id") { value("konvertering_til_pdf_error") }
            }
    }

    fun doPost(fil: File): ResultActionsDsl = mockMvc
        .multipart(URI(endpoint)) {
            file(createMockMultipartFile(fil))
        }

    fun createMockMultipartFile(fil: File) = MockMultipartFile(
        "file",
        fil.name,
        MediaType.MULTIPART_FORM_DATA.toString(),
        fil.readBytes()
    )

    fun doRequestAndVerifyExpectations(fil: File) {
        doPost(fil)
            .andExpect {
                status { isOk() }
                content { contentType(MimeTypes.APPLICATION_PDF) }
                header {
                    string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"${fil.nameWithoutExtension + ".pdf\""}"
                    )
                }
            }.andReturn().let {
                val mimeType = FileDetectionUtils.detectMimeType(it.response.contentAsByteArray)
                assertThat(mimeType).isEqualTo(MimeTypes.APPLICATION_PDF)
            }
    }
}

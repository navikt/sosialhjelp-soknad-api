package no.nav.sosialhjelp.soknad.vedlegg.fildetection

import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.TikaFileType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class FilDetectionUtilsTest {

    private val PDF_FILE = "sample_pdf.pdf"
    private val PNG_FILE = "sample_png.png"
    private val JPG_FILE = "sample_jpg.jpg"

    @Test
    fun `Test detect returnerer pdf`() {
        val tikaType = getAndDetectExampleFile(PDF_FILE)
        assertThat(tikaType.name).isEqualTo("PDF")
    }

    @Test
    fun `Test detect returnerer png`() {
        val tikaType = getAndDetectExampleFile(PNG_FILE)
        assertThat(tikaType.name).isEqualTo("PNG")
    }

    @Test
    fun `Test detect returnerer jpg`() {
        val tikaType = getAndDetectExampleFile(JPG_FILE)
        assertThat(tikaType.name).isEqualTo("JPEG")
    }

    @Test
    fun `Test filtype som ikke stottes`() {
        val tempFile = File.createTempFile("test", "file")
        val mimeType = FileDetectionUtils.detectMimeType(tempFile.readBytes())
        val tikaType = FileDetectionUtils.mapToTikaType(mimeType)
        assertThat(tikaType.name).isEqualTo("UNKNOWN")
    }

    private fun getAndDetectExampleFile(filename: String): TikaFileType {
        val url = this.javaClass.classLoader.getResource("eksempelfiler/$filename")?.file
        val exampleFile = File(url!!)
        val mimeType = FileDetectionUtils.detectMimeType(exampleFile.readBytes())
        return FileDetectionUtils.mapToTikaType(mimeType)
    }
}
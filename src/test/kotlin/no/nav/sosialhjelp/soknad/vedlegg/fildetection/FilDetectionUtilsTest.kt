package no.nav.sosialhjelp.soknad.vedlegg.fildetection

import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.BMP_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.GIF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.HEIC_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.HEIF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.JPG_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PNG_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.TEXT_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.TIF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.TikaFileType
import org.apache.commons.csv.CSVFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader

internal class FilDetectionUtilsTest {

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
    fun `Test detect heif returnerer heic`() {
        val mimeType = detectMimeType(HEIF_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/heic")
    }

    @Test
    fun `Test detect heic returnerer heif`() {
        val mimeType = detectMimeType(HEIC_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/heif")
    }

    @Test
    fun `Test detect returnerer tiff`() {
        val mimeType = detectMimeType(TIF_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/tiff")
    }

    @Test
    fun `Test detect returnerer gif`() {
        val mimeType = detectMimeType(GIF_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/gif")
    }

    @Test
    fun `Test detect returnerer bmp`() {
        val mimeType = detectMimeType(BMP_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/bmp")
    }

    @Test
    fun `Test detect returnerer excel`() {
        val mimeType = detectMimeType(EXCEL_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/gif")
    }

    @Test
    fun `Test detect returnerer word`() {
        val mimeType = detectMimeType(WORD_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/gif")
    }

    @Test
    fun `Test detect returnerer csv`() {
        val mimeType = detectMimeType(CSV_FILE.readBytes())
        assertThat(mimeType).isEqualTo("text/plain")
        val csvFormat = CSVFormat.Builder
            .create()
            .setDelimiter(";")
            .build()
    }

    @Test
    fun `Test detect returnerer text`() {
        val mimeType = detectMimeType(TEXT_FILE.readBytes())
        assertThat(mimeType).isEqualTo("text/plain")
        val csvFormat = CSVFormat.Builder
            .create()
            .setDelimiter(";")
            .build()

        val csvFile = CSV_FILE
        val extension = csvFile.extension

        val parsedTextFile = csvFormat.parse(FileReader(TEXT_FILE))
        val parsedCsvFile = csvFormat.parse(FileReader(CSV_FILE))

        parsedTextFile.firstEndOfLine

    }

    @Test
    fun `Test filtype som ikke stottes`() {
        val tempFile = File.createTempFile("test", "file")
        val mimeType = detectMimeType(tempFile.readBytes())
        val tikaType = FileDetectionUtils.mapToTikaType(mimeType)
        assertThat(tikaType.name).isEqualTo("UNKNOWN")
    }

    private fun getAndDetectExampleFile(exampleFile: File): TikaFileType {
        val mimeType = detectMimeType(exampleFile.readBytes())
        return FileDetectionUtils.mapToTikaType(mimeType)
    }
}

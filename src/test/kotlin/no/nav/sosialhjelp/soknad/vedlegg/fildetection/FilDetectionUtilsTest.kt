package no.nav.sosialhjelp.soknad.vedlegg.fildetection

import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.BMP_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.GIF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.HEIC_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.HEIF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.JPG_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PNG_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.TIF_FILE
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FilDetectionUtilsTest {
    @Test
    fun `Test detect returnerer pdf`() {
        val mimeType = detectMimeType(PDF_FILE.readBytes())
        assertThat(mimeType).isEqualTo("application/pdf")
    }

    @Test
    fun `Test detect returnerer png`() {
        val mimeType = detectMimeType(PNG_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/png")
    }

    @Test
    fun `Test detect returnerer jpg`() {
        val mimeType = detectMimeType(JPG_FILE.readBytes())
        assertThat(mimeType).isEqualTo("image/jpeg")
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
}

package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE_OLD
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE_OLD
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes.APPLICATION_PDF
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.CSV
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.EXCEL
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.FiltypeUtil.finnKonverterer
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.WORD
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KonverteringTest {
    @Test
    fun `Test konverter excel-fil (xlsx) stottes`() {
        val stottetFiltype = finnKonverterer(detectMimeType(EXCEL_FILE.readBytes()), EXCEL_FILE.name)
        assertThat(stottetFiltype).isEqualTo(EXCEL)

        val konvertertFilBytes = stottetFiltype!!.getFiltypeConverter().konverterTilPdf(EXCEL_FILE.readBytes())
        assertThat(detectMimeType(konvertertFilBytes)).isEqualTo(APPLICATION_PDF)
    }

    @Test
    fun `Test excel-fil (xls) stottes ikke`() {
        val stottetFiltype = finnKonverterer(detectMimeType(EXCEL_FILE_OLD.readBytes()), EXCEL_FILE_OLD.name)
        assertThat(stottetFiltype).isNull()
    }

    @Test
    fun `Test word-fil (docx) stottes`() {
        val stottetFiltype = finnKonverterer(detectMimeType(WORD_FILE.readBytes()), WORD_FILE.name)
        assertThat(stottetFiltype).isEqualTo(WORD)

        val konvertertFilBytes = stottetFiltype!!.getFiltypeConverter().konverterTilPdf(WORD_FILE.readBytes())
        assertThat(detectMimeType(konvertertFilBytes)).isEqualTo(APPLICATION_PDF)
    }

    @Test
    fun `Test word-fil (doc) stottes ikke`() {
        val stottetFiltype = finnKonverterer(detectMimeType(WORD_FILE_OLD.readBytes()), WORD_FILE_OLD.name)
        assertThat(stottetFiltype).isNull()
    }

    @Test
    fun `Test csv-fil (csv) stottes`() {
        val stottetFiltype = finnKonverterer(detectMimeType(CSV_FILE.readBytes()), CSV_FILE.name)
        assertThat(stottetFiltype).isEqualTo(CSV)

        val konvertertPdfBytes = stottetFiltype!!.getFiltypeConverter().konverterTilPdf(CSV_FILE.readBytes())
        assertThat(detectMimeType(konvertertPdfBytes)).isEqualTo(APPLICATION_PDF)
    }
}

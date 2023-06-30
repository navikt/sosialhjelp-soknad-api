package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.csv.CsvToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.CsvKonverteringException
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.ExcelKonverteringException
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.WordKonverteringException
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.word.WordToPdfConverter
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.TEXT_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.KonverteringTilPdfException
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.FilKonvertering.konverterHvisStottet
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.FiltypeUtil.finnKonverterer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class FilKonverteringTest {

    @Test
    fun `Konverter fil hvis stottet format`() {
        val orgNavn = EXCEL_FILE.name
        val orgData = EXCEL_FILE.readBytes()

        val (filnavn, data) = konverterHvisStottet(orgNavn, orgData)
        assertThat(orgNavn).isNotEqualTo(filnavn)
        assertThat(filnavn).contains(".pdf")
        assertThat(detectMimeType(data)).isEqualTo("application/pdf")
    }

    @Test
    fun `Fil rores ikke hvis formatet ikke er stottet`() {
        val orgNavn = TEXT_FILE.name
        val orgData = TEXT_FILE.readBytes()

        val (filnavn, data) = konverterHvisStottet(orgNavn, orgData)
        assertThat(orgNavn).isEqualTo(filnavn)
        assertThat(orgData).isEqualTo(data)
        assertThat(detectMimeType(orgData)).isEqualTo(detectMimeType(data))
    }

    @Test
    fun `Test konverter excel-fil (xlsx) stottes`() {
        val stottetFiltype = finnKonverterer(detectMimeType(EXCEL_FILE.readBytes()), EXCEL_FILE.name)
        assertThat(stottetFiltype).isEqualTo(StottetFiltype.EXCEL)

        val konvertertFilBytes = stottetFiltype!!.getFiltypeConverter().konverterTilPdf(EXCEL_FILE.readBytes())
        assertThat(detectMimeType(konvertertFilBytes)).isEqualTo(MimeTypes.APPLICATION_PDF)
    }

    @Test
    fun `Test excel-fil (xls) stottes ikke`() {
        val stottetFiltype = finnKonverterer(
            detectMimeType(ExampleFileRepository.EXCEL_FILE_OLD.readBytes()),
            ExampleFileRepository.EXCEL_FILE_OLD.name
        )
        assertThat(stottetFiltype).isNull()
    }

    @Test
    fun `Test word-fil (docx) stottes`() {
        val stottetFiltype = finnKonverterer(detectMimeType(WORD_FILE.readBytes()), WORD_FILE.name)
        assertThat(stottetFiltype).isEqualTo(StottetFiltype.WORD)

        val konvertertFilBytes = stottetFiltype!!.getFiltypeConverter().konverterTilPdf(WORD_FILE.readBytes())
        assertThat(detectMimeType(konvertertFilBytes)).isEqualTo(MimeTypes.APPLICATION_PDF)
    }

    @Test
    fun `Test word-fil (doc) stottes ikke`() {
        val stottetFiltype = finnKonverterer(
            detectMimeType(ExampleFileRepository.WORD_FILE_OLD.readBytes()),
            ExampleFileRepository.WORD_FILE_OLD.name
        )
        assertThat(stottetFiltype).isNull()
    }

    @Test
    fun `Test csv-fil (csv) stottes`() {
        val stottetFiltype = finnKonverterer(detectMimeType(CSV_FILE.readBytes()), CSV_FILE.name)
        assertThat(stottetFiltype).isEqualTo(StottetFiltype.CSV)

        val konvertertPdfBytes = stottetFiltype!!.getFiltypeConverter().konverterTilPdf(CSV_FILE.readBytes())
        assertThat(detectMimeType(konvertertPdfBytes)).isEqualTo(MimeTypes.APPLICATION_PDF)
    }

    @Test
    fun `Kaster KonverteringException hvis konvertering feiler`() {

        mockkObject(ExcelToPdfConverter)
        mockkObject(WordToPdfConverter)
        mockkObject(CsvToPdfConverter)

        every { ExcelToPdfConverter.konverterTilPdf(any()) } throws ExcelKonverteringException("feil", null)
        every { WordToPdfConverter.konverterTilPdf(any()) } throws WordKonverteringException("feil", null)
        every { CsvToPdfConverter.konverterTilPdf(any()) } throws CsvKonverteringException("feil", null)

        assertThatThrownBy { konverterHvisStottet(EXCEL_FILE.name, EXCEL_FILE.readBytes()) }
            .isInstanceOf(KonverteringTilPdfException::class.java)
            .hasCauseInstanceOf(ExcelKonverteringException::class.java)

        assertThatThrownBy { konverterHvisStottet(WORD_FILE.name, WORD_FILE.readBytes()) }
            .isInstanceOf(KonverteringTilPdfException::class.java)
            .hasCauseInstanceOf(WordKonverteringException::class.java)

        assertThatThrownBy { konverterHvisStottet(CSV_FILE.name, CSV_FILE.readBytes()) }
            .isInstanceOf(KonverteringTilPdfException::class.java)
            .hasCauseInstanceOf(CsvKonverteringException::class.java)

        clearAllMocks()
    }
}

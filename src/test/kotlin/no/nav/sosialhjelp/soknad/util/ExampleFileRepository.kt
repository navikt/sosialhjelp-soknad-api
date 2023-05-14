package no.nav.sosialhjelp.soknad.util

import java.io.File

object ExampleFileRepository {

    val BMP_FILE = getFile("sample_bmp.bmp")
    val PDF_FILE = getFile("sample_pdf.pdf")
    val PNG_FILE = getFile("sample_png.png")
    val JPG_FILE = getFile("sample_jpg.jpg")
    val HEIC_FILE = getFile("sample_heic.heic")
    val HEIF_FILE = getFile("sample_heif.heif")
    val TIF_FILE = getFile("sample_tif.tif")
    val GIF_FILE = getFile("sample_gif.gif")

    val WORD_FILE = getFile("sample_word.docx")
    val WORD_FILE_OLD = getFile("sample_word_old.doc")
    val EXCEL_FILE = getFile("sample_excel.xlsx")
    val EXCEL_FILE_OLD = getFile("sample_excel_old.xls")

    val CSV_FILE = getFile("sample_csv.csv")
    val TEXT_FILE = getFile("sample_text.txt")

    fun getFile(filename: String): File {
        val url = this.javaClass.classLoader.getResource("eksempelfiler/$filename")?.file
        return File(url!!)
    }
}

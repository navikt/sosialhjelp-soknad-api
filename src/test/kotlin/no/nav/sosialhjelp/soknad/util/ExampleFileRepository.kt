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

    val EXCEL_FILE = getFile("sample_excel.xlsx")

    fun getFile(filename: String): File {
        val url = this.javaClass.classLoader.getResource("eksempelfiler/$filename")?.file
        return File(url!!)
    }
}

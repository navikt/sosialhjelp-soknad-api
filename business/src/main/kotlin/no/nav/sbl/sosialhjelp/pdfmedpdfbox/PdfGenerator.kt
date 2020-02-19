package no.nav.sbl.sosialhjelp.pdfmedpdfbox

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class PdfGenerator {
    private val document = PDDocument()
    private val completedPages: ArrayList<PDPage>? = null
    private var currentPage = PDPage(PDRectangle.A4)
    private val completedStreams: ArrayList<PDPageContentStream>? = null
    private var currentStream: PDPageContentStream
    private var y: Float
    @kotlin.jvm.Throws(IOException::class)
    fun finish(): ByteArray { // Add current page to document
        document.addPage(currentPage)
        // Close remaining streams
        // save document to byte array output stream and return byte array
        val baos = ByteArrayOutputStream()
        currentStream.close()
        document.save(baos)
        val pdf: ByteArray
        pdf = baos.toByteArray()
        return pdf
    }

    @kotlin.jvm.Throws(IOException::class)
    private fun continueOnNewPage() { // add page to doc
        document.addPage(currentPage)
        // close stream
        currentStream.close()
        // add new page and stream
        currentPage = PDPage(PDRectangle.A4)
        currentStream = PDPageContentStream(document, currentPage)
        // reset y
        y = calculateStartY()
    }

    fun addBlankLine() {
        y -= 20f
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivTekst(text: String?) {
        addParagraph(text, FONT_PLAIN, FONT_PLAIN_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivTekstKursiv(text: String?) {
        addParagraph(text, FONT_KURSIV, FONT_PLAIN_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivTekstMedInnrykk(text: String?, innrykk: Int) {
        addParagraph(text, FONT_PLAIN, FONT_PLAIN_SIZE.toFloat(), innrykk)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivTekstBold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_PLAIN_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH1(tekst: String?) {
        addParagraph(tekst, FONT_PLAIN, FONT_H1_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH1Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H1_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH2(tekst: String?) {
        addParagraph(tekst, FONT_PLAIN, FONT_H2_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH2Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H2_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH3(tekst: String?) {
        addParagraph(tekst, FONT_PLAIN, FONT_H3_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH3Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H3_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH4(tekst: String?) {
        addParagraph(tekst, FONT_PLAIN, FONT_H4_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun skrivH4Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H4_SIZE.toFloat(), MARGIN)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun addCenteredH1Bold(heading: String?) {
        addCenteredParagraph(heading, FONT_BOLD, FONT_H1_SIZE.toFloat(), LEADING_PERCENTAGE)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun addCenteredH4Bold(heading: String?) {
        addCenteredParagraph(heading, FONT_BOLD, FONT_H4_SIZE.toFloat(), LEADING_PERCENTAGE)
    }

    @kotlin.jvm.Throws(IOException::class)
    fun addDividerLine() {
        currentStream.setLineWidth(1f)
        currentStream.moveTo(MARGIN.toFloat(), y)
        currentStream.lineTo(MEDIA_BOX.width - MARGIN, y)
        currentStream.closeAndStroke()
    }

    @kotlin.jvm.Throws(IOException::class)
    fun addParagraph(
            text: String?,
            font: PDFont,
            fontSize: Float,
            margin: Int
    ) {
        val justify = false
        val lines = parseLines(text, font, fontSize)
        currentStream.setFont(font, fontSize)
        currentStream.beginText()
        currentStream.newLineAtOffset(margin.toFloat(), y)
        for (i in lines.indices) {
            val line = lines[i]
            var charSpacing = 0f
            if (justify) {
                if (line.length > 1) {
                    val size = fontSize * font.getStringWidth(line) / 1000
                    val free = WIDTH_OF_CONTENT_COLUMN - size
                    if (free > 0 && lines[lines.size - 1] != line) {
                        charSpacing = free / (line.length - 1)
                    }
                }
            }
            currentStream.setCharacterSpacing(charSpacing)
            currentStream.showText(line)
            currentStream.newLineAtOffset(0f, -LEADING_PERCENTAGE * fontSize)
            y -= fontSize * LEADING_PERCENTAGE
            if (y < 100) {
                currentStream.endText()
                continueOnNewPage()
                currentStream.beginText()
                currentStream.setFont(font, fontSize)
                currentStream.newLineAtOffset(margin.toFloat(), y)
            }
        }
        currentStream.endText()
    }

    @kotlin.jvm.Throws(IOException::class)
    fun addCenteredParagraph(
            heading: String?,
            font: PDFont,
            fontSize: Float,
            leadingPercentage: Float
    ) {
        val lines = parseLines(heading, font, fontSize)
        currentStream.beginText()
        currentStream.setFont(font, fontSize)
        var prevX = 0f
        for (i in lines.indices) {
            prevX = if (i == 0) {
                val lineWidth = font.getStringWidth(lines[i]) / 1000 * fontSize
                val startX = (MEDIA_BOX.width - lineWidth) / 2
                currentStream.newLineAtOffset(startX, y)
                startX
            } else {
                val lineWidth = font.getStringWidth(lines[i]) / 1000 * fontSize
                val startX = (MEDIA_BOX.width - lineWidth) / 2
                currentStream.newLineAtOffset(startX - prevX, -leadingPercentage * fontSize)
                startX
            }
            currentStream.showText(lines[i])
        }
        currentStream.endText()
        y -= lines.size * fontSize
    }

    @kotlin.jvm.Throws(IOException::class)
    fun addLogo() {
        val ximage = PDImageXObject.createFromByteArray(document, logo(), "logo")
        val startX = (MEDIA_BOX.width - 99) / 2
        val offsetTop = 40f
        currentStream.drawImage(ximage, 27f, 765f, 99f, 62f)
    }

    companion object {
        const val MARGIN = 40
        const val INNRYKK_1 = 50
        const val INNRYKK_2 = 60
        const val INNRYKK_3 = 70
        const val INNRYKK_4 = 80
        val FONT_PLAIN: PDFont = PDType1Font.HELVETICA
        val FONT_KURSIV: PDFont = PDType1Font.HELVETICA_OBLIQUE
        val FONT_BOLD: PDFont = PDType1Font.HELVETICA_BOLD
        const val FONT_PLAIN_SIZE = 12
        const val FONT_H1_SIZE = 20
        const val FONT_H2_SIZE = 18
        const val FONT_H3_SIZE = 16
        const val FONT_H4_SIZE = 14
        private val fontPLainHeight = Math.round(FONT_PLAIN.fontDescriptor.fontBoundingBox.height / 1000 * FONT_PLAIN_SIZE)
        private val fontHeadingHeight = Math.round(FONT_PLAIN.fontDescriptor.fontBoundingBox.height / 1000 * FONT_H1_SIZE)
        val MEDIA_BOX = PDPage(PDRectangle.A4).mediaBox
        val WIDTH_OF_CONTENT_COLUMN = PDPage(PDRectangle.A4).mediaBox.width - MARGIN * 2
        private const val DEFAULT_USER_SPACE_UNIT_DPI = 72
        private const val MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI
        const val PAGE_WIDTH = 210 * MM_TO_UNITS
        const val LEADING_PERCENTAGE = 1.5f
        fun calculateStartY(): Float {
            return MEDIA_BOX.upperRightY - MARGIN
        }

        @kotlin.jvm.Throws(IOException::class)
        private fun parseLines(text: String?, font: PDFont, fontSize: Float): List<String> {
            var text = text
            val lines: MutableList<String> = ArrayList()
            var lastSpace = -1
            while (text != null && text.length > 0) {
                var spaceIndex: Int = text.indexOf(' ', lastSpace + 1)
                if (spaceIndex < 0) spaceIndex = text.length
                var subString: String = text.substring(0, spaceIndex)
                val size = fontSize * font.getStringWidth(subString) / 1000
                if (size > WIDTH_OF_CONTENT_COLUMN) {
                    if (lastSpace < 0) {
                        lastSpace = spaceIndex
                    }
                    subString = text.substring(0, lastSpace)
                    lines.add(subString)
                    text = text.substring(lastSpace).trim({ it <= ' ' })
                    lastSpace = -1
                } else if (spaceIndex == text.length) {
                    lines.add(text)
                    text = ""
                } else {
                    lastSpace = spaceIndex
                }
            }
            return lines
        }

        private fun logo(): ByteArray {
            try {
                val classPathResource = ClassPathResource("/pdf/nav-logo_alphaless.png")
                val inputStream = classPathResource.inputStream
                return StreamUtils.copyToByteArray(inputStream)
            } catch (e: IOException) { // FIXME: Handle it
                e.printStackTrace()
            }
            return ByteArray(0)
        }
    }

    init {
        currentStream = PDPageContentStream(document, currentPage)
        y = calculateStartY()
        addLogo()
    }
}
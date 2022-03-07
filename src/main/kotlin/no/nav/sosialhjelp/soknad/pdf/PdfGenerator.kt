package no.nav.sosialhjelp.soknad.pdf

import com.vdurmont.emoji.EmojiParser
import org.apache.jempbox.xmp.XMPMetadata
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDMetadata
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import java.io.ByteArrayOutputStream
import java.io.IOException

@Component
class PdfGenerator {
    private val logger = LoggerFactory.getLogger(PdfGenerator::class.java)

    private val document = PDDocument()
    private val completedPages: ArrayList<PDPage>? = null
    private var currentPage = PDPage(PDRectangle.A4)
    private val completedStreams: ArrayList<PDPageContentStream>? = null
    private var currentStream: PDPageContentStream
    private var y: Float

    private val FONT_REGULAR: PDFont = PDType0Font.load(document, ClassPathResource(REGULAR).inputStream)
    private val FONT_BOLD: PDFont = PDType0Font.load(document, ClassPathResource(BOLD).inputStream)
    private val FONT_KURSIV: PDFont = PDType0Font.load(document, ClassPathResource(KURSIV).inputStream)

    private val xmp = XMPMetadata()
    private val pdfaid = XMPSchemaPDFAId(xmp)

    private val colorProfile = ClassPathResource("sRGB.icc").inputStream
    private val oi = PDOutputIntent(document, colorProfile)

    private val cat = document.documentCatalog
    private val metadata = PDMetadata(document)

    init {
        currentStream = PDPageContentStream(document, currentPage)
        y = calculateStartY()
        addLogo()
    }

    fun finish(): ByteArray {
        cat.metadata = metadata

        xmp.addSchema(pdfaid)
        pdfaid.conformance = "B"
        pdfaid.part = 1
        pdfaid.about = ""
        try {
            metadata.importXMPMetadata(xmp.asByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        oi.info = "sRGB IEC61966-2.1"
        oi.outputCondition = "sRGB IEC61966-2.1"
        oi.outputConditionIdentifier = "sRGB IEC61966-2.1"
        oi.registryName = "http://www.color.org"
        cat.addOutputIntent(oi)

        // Add current page to document
        document.addPage(currentPage)
        // Close remaining streams
        // save document to byte array output stream and return byte array
        val baos = ByteArrayOutputStream()
        currentStream.close()
        document.save(baos)
        document.close()
        return baos.toByteArray()
    }

    private fun continueOnNewPage() {
        // add page to doc
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

    fun skrivTekst(text: String?) {
        addParagraph(text, FONT_REGULAR, FONT_PLAIN_SIZE.toFloat(), MARGIN)
    }

    fun skrivTekstKursiv(text: String?) {
        addParagraph(text, FONT_KURSIV, FONT_PLAIN_SIZE.toFloat(), MARGIN)
    }

    fun skrivTekstMedInnrykk(text: String?, innrykk: Int) {
        addParagraph(text, FONT_REGULAR, FONT_PLAIN_SIZE.toFloat(), innrykk)
    }

    fun skrivTekstBold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_PLAIN_SIZE.toFloat(), MARGIN)
    }

    fun skrivH1(tekst: String?) {
        addParagraph(tekst, FONT_REGULAR, FONT_H1_SIZE.toFloat(), MARGIN)
    }

    fun skrivH1Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H1_SIZE.toFloat(), MARGIN)
    }

    fun skrivH2(tekst: String?) {
        addParagraph(tekst, FONT_REGULAR, FONT_H2_SIZE.toFloat(), MARGIN)
    }

    fun skrivH2Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H2_SIZE.toFloat(), MARGIN)
    }

    fun skrivH3(tekst: String?) {
        addParagraph(tekst, FONT_REGULAR, FONT_H3_SIZE.toFloat(), MARGIN)
    }

    fun skrivH3Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H3_SIZE.toFloat(), MARGIN)
    }

    fun skrivH4(tekst: String?) {
        addParagraph(tekst, FONT_REGULAR, FONT_H4_SIZE.toFloat(), MARGIN)
    }

    fun skrivH4Bold(tekst: String?) {
        addParagraph(tekst, FONT_BOLD, FONT_H4_SIZE.toFloat(), MARGIN)
    }

    fun addCenteredH1Bold(heading: String?) {
        addCenteredParagraph(heading, FONT_BOLD, FONT_H1_SIZE.toFloat(), LEADING_PERCENTAGE)
    }

    fun addCenteredH4Bold(heading: String?) {
        addCenteredParagraph(heading, FONT_BOLD, FONT_H4_SIZE.toFloat(), LEADING_PERCENTAGE)
    }

    fun addDividerLine() {
        currentStream.setLineWidth(1f)
        currentStream.moveTo(MARGIN.toFloat(), y)
        currentStream.lineTo(MEDIA_BOX.width - MARGIN, y)
        currentStream.closeAndStroke()
    }

    fun addParagraph(text: String?, font: PDFont, fontSize: Float, margin: Int) {
        val lines = parseLines(text, font, fontSize)
        currentStream.setFont(font, fontSize)
        currentStream.beginText()
        currentStream.newLineAtOffset(margin.toFloat(), y)

        lines.forEach { line ->
            val charSpacing = 0f

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

    fun addCenteredParagraph(heading: String?, font: PDFont, fontSize: Float, leadingPercentage: Float) {
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

    private fun parseLines(text: String?, font: PDFont, fontSize: Float): List<String> {
        var text = text
        val lines: MutableList<String> = ArrayList()
        if (text == null) {
            return lines
        }
        text = EmojiParser.parseToAliases(text)
        splitTextOnNewlines(text).forEach { originalLine ->
            var lastSpace = -1
            var line = originalLine
            while (line.isNotEmpty()) {
                var spaceIndex = line.indexOf(' ', lastSpace + 1)
                if (spaceIndex < 0) spaceIndex = line.length
                var subString = line.substring(0, spaceIndex)
                var size = fontSize * font.getStringWidth(subString) / 1000
                if (size > WIDTH_OF_CONTENT_COLUMN) {
                    if (lastSpace < 0) {
                        lastSpace = spaceIndex
                    }
                    subString = line.substring(0, lastSpace)
                    size = fontSize * font.getStringWidth(subString) / 1000
                    // Noen ord, eks en del URLer, er for lange til å kunne passe på en enkelt linje. Vi deler de opp slik at mest mulig av ordet får plass på en linje. "enkel" kode over lesbarhet.
                    if (size > WIDTH_OF_CONTENT_COLUMN) {
                        val word = subString
                        var lastSplit = 0
                        for (i in 0 until word.length - 1) {
                            // Check if next character will make line too long
                            if (fontSize * font.getStringWidth(word.substring(lastSplit, i + 1)) / 1000 > WIDTH_OF_CONTENT_COLUMN) {
                                lines.add(word.substring(lastSplit, i))
                                lastSplit = i
                            }
                        }
                        lines.add(word.substring(lastSplit))
                    } else {
                        lines.add(subString)
                    }
                    line = line.substring(lastSpace).trim { it <= ' ' }
                    lastSpace = -1
                } else if (spaceIndex == line.length) {
                    lines.add(line)
                    line = ""
                } else {
                    lastSpace = spaceIndex
                }
            }
        }
        return lines
    }

    private fun splitTextOnNewlines(text: String?): List<String> {
        val splitByNewlines: MutableList<String> = ArrayList()
        var stringBuilder = StringBuilder()
        for (i in 0 until text!!.length) {
            if (characterIsLinebreak(text.codePointAt(i))) {
                splitByNewlines.add(stringBuilder.toString())
                stringBuilder = StringBuilder()
            } else if (characterIsLegal(text.codePointAt(i))) {
                stringBuilder.append(text[i])
            } else {
                logger.info("Prøver å skrive ulovlig tegn til pdf. UTF-8 codepoint: {}", text.codePointAt(i))
            }
        }
        splitByNewlines.add(stringBuilder.toString())
        return splitByNewlines
    }

    private fun characterIsLegal(codePoint: Int): Boolean {
        return codePoint in BASIC_LATIN_START..BASIC_LATIN_END || codePoint in EXTENDED_LATIN_START..EXTENDED_LATIN_END
    }

    private fun characterIsLinebreak(codePoint: Int): Boolean {
        return codePoint == 0x000A || codePoint == 0x000D
    }

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

        private const val REGULAR = "/fonts/Source_Sans_Pro/SourceSansPro-Regular.ttf"
        private const val KURSIV = "/fonts/Source_Sans_Pro/SourceSansPro-Italic.ttf"
        private const val BOLD = "/fonts/Source_Sans_Pro/SourceSansPro-Bold.ttf"

        const val FONT_PLAIN_SIZE = 12
        const val FONT_H1_SIZE = 20
        const val FONT_H2_SIZE = 18
        const val FONT_H3_SIZE = 16
        const val FONT_H4_SIZE = 14

        val MEDIA_BOX = PDPage(PDRectangle.A4).mediaBox
        val WIDTH_OF_CONTENT_COLUMN = PDPage(PDRectangle.A4).mediaBox.width - MARGIN * 2

        private const val DEFAULT_USER_SPACE_UNIT_DPI = 72
        private const val MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI
        const val PAGE_WIDTH = 210 * MM_TO_UNITS
        const val LEADING_PERCENTAGE = 1.5f

        const val BASIC_LATIN_START = 0x0020
        const val BASIC_LATIN_END = 0x007E

        const val EXTENDED_LATIN_START = 0x00A0
        const val EXTENDED_LATIN_END = 0x0170

        fun calculateStartY(): Float {
            return MEDIA_BOX.upperRightY - MARGIN
        }

        private fun logo(): ByteArray {
            try {
                val classPathResource = ClassPathResource("/pdf/nav-logo_alphaless.jpg")
                val inputStream = classPathResource.inputStream
                return StreamUtils.copyToByteArray(inputStream)
            } catch (e: IOException) {
                // FIXME: Handle it
                e.printStackTrace()
            }
            return ByteArray(0)
        }
    }
}

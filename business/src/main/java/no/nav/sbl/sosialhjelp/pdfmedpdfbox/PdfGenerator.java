package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.cxf.common.logging.LogUtils.getLogger;

public class PdfGenerator {

    private final Logger logger = getLogger(PdfGenerator.class);

    public static final int MARGIN = 40;
    public static final int INNRYKK_1 = 50;
    public static final int INNRYKK_2 = 60;
    public static final int INNRYKK_3 = 70;
    public static final int INNRYKK_4 = 80;

    private static final String REGULAR = "/fonts/Source_Sans_Pro/SourceSansPro-Regular.ttf";
    private static final String KURSIV = "/fonts/Source_Sans_Pro/SourceSansPro-Italic.ttf";
    private static final String BOLD = "/fonts/Source_Sans_Pro/SourceSansPro-Bold.ttf";

    public static final int FONT_PLAIN_SIZE = 12;
    public static final int FONT_H1_SIZE = 20;
    public static final int FONT_H2_SIZE = 18;
    public static final int FONT_H3_SIZE = 16;
    public static final int FONT_H4_SIZE = 14;

    public static final PDRectangle MEDIA_BOX = new PDPage(PDRectangle.A4).getMediaBox();
    public static final float WIDTH_OF_CONTENT_COLUMN = new PDPage(PDRectangle.A4).getMediaBox().getWidth() - MARGIN * 2;

    private static final int DEFAULT_USER_SPACE_UNIT_DPI = 72;
    private static final float MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI;
    public static final float PAGE_WIDTH = 210 * MM_TO_UNITS;
    public static final float LEADING_PERCENTAGE = 1.5f;

    public static final int BASIC_LATIN_START = 0x0020;
    public static final int BASIC_LATIN_END = 0x007E;

    public static final int EXTENDED_LATIN_START = 0x00A0;
    public static final int EXTENDED_LATIN_END = 0x0170;

    private PDDocument document = new PDDocument();
    private ArrayList<PDPage> completedPages;
    private PDPage currentPage = new PDPage(PDRectangle.A4);
    private ArrayList<PDPageContentStream> completedStreams;
    private PDPageContentStream currentStream;
    private float y;

    public PdfGenerator() throws IOException {
        this.currentStream = new PDPageContentStream(document, currentPage);
        this.y = calculateStartY();
        this.addLogo();
    }

    public byte[] finish() throws IOException {
        // Add current page to document
        this.document.addPage(this.currentPage);
        // Close remaining streams
        // save document to byte array output stream and return byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.currentStream.close();
        this.document.save(baos);
        this.document.close();
        byte[] pdf;
        pdf = baos.toByteArray();
        return pdf;
    }

    private void continueOnNewPage() throws IOException {
        // add page to doc
        this.document.addPage(this.currentPage);

        // close stream
        this.currentStream.close();

        // add new page and stream
        this.currentPage = new PDPage(PDRectangle.A4);
        this.currentStream = new PDPageContentStream(this.document, this.currentPage);

        // reset y
        this.y = calculateStartY();
    }

    public void addBlankLine() {
        this.y -= 20;
    }


    public static float calculateStartY() {
        return MEDIA_BOX.getUpperRightY() - MARGIN;
    }

    public void skrivTekst(String text) throws IOException {
        this.addParagraph(text, REGULAR, FONT_PLAIN_SIZE, MARGIN);
    }

    public void skrivTekstKursiv(String text) throws IOException {
        this.addParagraph(text, KURSIV, FONT_PLAIN_SIZE, MARGIN);
    }

    public void skrivTekstMedInnrykk(String text, int innrykk) throws IOException {
        this.addParagraph(text, REGULAR, FONT_PLAIN_SIZE, innrykk);
    }

    public void skrivTekstBold(String tekst) throws IOException {
        this.addParagraph(tekst, BOLD, FONT_PLAIN_SIZE, MARGIN);
    }

    public void skrivH1(String tekst) throws IOException {
        this.addParagraph(tekst, REGULAR, FONT_H1_SIZE, MARGIN);
    }

    public void skrivH1Bold(String tekst) throws IOException {
        this.addParagraph(tekst, BOLD, FONT_H1_SIZE, MARGIN);
    }

    public void skrivH2(String tekst) throws IOException {
        this.addParagraph(tekst, REGULAR, FONT_H2_SIZE, MARGIN);
    }

    public void skrivH2Bold(String tekst) throws IOException {
        this.addParagraph(tekst, BOLD, FONT_H2_SIZE, MARGIN);
    }

    public void skrivH3(String tekst) throws IOException {
        this.addParagraph(tekst, REGULAR, FONT_H3_SIZE, MARGIN);
    }

    public void skrivH3Bold(String tekst) throws IOException {
        this.addParagraph(tekst, BOLD, FONT_H3_SIZE, MARGIN);
    }

    public void skrivH4(String tekst) throws IOException {
        this.addParagraph(tekst, REGULAR, FONT_H4_SIZE, MARGIN);
    }

    public void skrivH4Bold(String tekst) throws IOException {
        this.addParagraph(tekst, BOLD, FONT_H4_SIZE, MARGIN);
    }

    public void addCenteredH1Bold(String heading) throws IOException {
        addCenteredParagraph(heading, BOLD, FONT_H1_SIZE, LEADING_PERCENTAGE);
    }

    public void addCenteredH4Bold(String heading) throws IOException {
        addCenteredParagraph(heading, BOLD, FONT_H4_SIZE, LEADING_PERCENTAGE);
    }


    public void addDividerLine() throws IOException {
        this.currentStream.setLineWidth(1);
        this.currentStream.moveTo(MARGIN, this.y);
        this.currentStream.lineTo(MEDIA_BOX.getWidth() - MARGIN, this.y);
        this.currentStream.closeAndStroke();
    }

    public void addParagraph(
            String text,
            String fontType,
            float fontSize,
            int margin
    ) throws IOException {

        PDType0Font font = PDType0Font.load(document, new ClassPathResource(fontType).getFile());

        List<String> lines = parseLines(text, font, fontSize);
        this.currentStream.setFont(font, fontSize);
        this.currentStream.beginText();
        this.currentStream.newLineAtOffset(margin, this.y);

        for (String line : lines) {
            float charSpacing = 0;

            this.currentStream.setCharacterSpacing(charSpacing);
            this.currentStream.showText(line);
            this.currentStream.newLineAtOffset(0, -LEADING_PERCENTAGE * fontSize);

            this.y -= fontSize * LEADING_PERCENTAGE;
            if (this.y < 100) {
                this.currentStream.endText();
                this.continueOnNewPage();
                this.currentStream.beginText();
                this.currentStream.setFont(font, fontSize);
                this.currentStream.newLineAtOffset(margin, this.y);
            }
        }
        this.currentStream.endText();
    }

    public void addCenteredParagraph(
            String heading,
            String fontType,
            float fontSize,
            float leadingPercentage
    ) throws IOException {

        PDType0Font font = PDType0Font.load(document, new ClassPathResource(fontType).getFile());

        List<String> lines = parseLines(heading, font, fontSize);
        this.currentStream.beginText();
        this.currentStream.setFont(font, fontSize);

        float prevX = 0;

        for (int i = 0; i < lines.size(); i++) {

            if (i == 0) {
                float lineWidth = font.getStringWidth(lines.get(i)) / 1000 * fontSize;
                float startX = (MEDIA_BOX.getWidth() - lineWidth) / 2;
                this.currentStream.newLineAtOffset(startX, this.y);
                prevX = startX;
            } else {
                float lineWidth = font.getStringWidth(lines.get(i)) / 1000 * fontSize;
                float startX = (MEDIA_BOX.getWidth() - lineWidth) / 2;
                this.currentStream.newLineAtOffset(startX - prevX, -leadingPercentage * fontSize);
                prevX = startX;
            }

            this.currentStream.showText(lines.get(i));
        }
        this.currentStream.endText();

        this.y -= lines.size() * fontSize;
    }

    private List<String> parseLines(String text, PDFont font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();

        for (String line : splitTextOnNewlines(text)) {
            int lastSpace = -1;
            while (line != null && line.length() > 0) {
                int spaceIndex = line.indexOf(' ', lastSpace + 1);
                if (spaceIndex < 0)
                    spaceIndex = line.length();
                String subString = line.substring(0, spaceIndex);
                float size = fontSize * font.getStringWidth(subString) / 1000;
                if (size > PdfGenerator.WIDTH_OF_CONTENT_COLUMN) {
                    if (lastSpace < 0) {
                        lastSpace = spaceIndex;
                    }
                    subString = line.substring(0, lastSpace);
                    lines.add(subString);
                    line = line.substring(lastSpace).trim();
                    lastSpace = -1;
                } else if (spaceIndex == line.length()) {
                    lines.add(line);
                    line = "";
                } else {
                    lastSpace = spaceIndex;
                }
            }
        }

        return lines;
    }

    private List<String> splitTextOnNewlines(String text) {
        List<String> splitByNewlines = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (characterIsLinebreak(text.codePointAt(i))) {
                splitByNewlines.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            } else if (characterIsLegal(text.codePointAt(i))) {
                stringBuilder.append(text.charAt(i));
            } else {
                logger.info("Prøver å skrive ulovlig tegn til pdf. UTF-8 codepoint: " + text.codePointAt(i));
            }
        }
        splitByNewlines.add(stringBuilder.toString());
        return splitByNewlines;
    }

    private boolean characterIsLegal(int codePoint) {
        if ((codePoint >= BASIC_LATIN_START && codePoint <= BASIC_LATIN_END) || (codePoint >= EXTENDED_LATIN_START && codePoint <= EXTENDED_LATIN_END)) {
            return true;
        }
        return false;
    }

    private boolean characterIsLinebreak(int codePoint) {
        return codePoint == 0x000A || codePoint == 0x000D;
    }

    public void addLogo() throws IOException {
        PDImageXObject ximage = PDImageXObject.createFromByteArray(this.document, logo(), "logo");
        float startX = (MEDIA_BOX.getWidth() - 99) / 2;
        float offsetTop = 40;
        this.currentStream.drawImage(ximage, 27, 765, 99, 62);
    }

    private static byte[] logo() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("/pdf/nav-logo_alphaless.png");
            InputStream inputStream = classPathResource.getInputStream();
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);
            return bytes;
        } catch (IOException e) {
            // FIXME: Handle it
            e.printStackTrace();
        }
        return new byte[0];
    }
}
package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfGenerator {

    public static final int MARGIN = 40;
    public static final int INNRYKK_1 = 50;
    public static final int INNRYKK_2 = 60;
    public static final int INNRYKK_3 = 70;
    public static final int INNRYKK_4 = 80;

    public static final PDFont FONT_PLAIN = PDType1Font.HELVETICA;
    public static final PDFont FONT_KURSIV = PDType1Font.HELVETICA_OBLIQUE;
    public static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;

    public static final int FONT_PLAIN_SIZE = 12;
    public static final int FONT_H1_SIZE = 20;
    public static final int FONT_H2_SIZE = 18;
    public static final int FONT_H3_SIZE = 16;
    public static final int FONT_H4_SIZE = 14;
    private static final int fontPLainHeight =
            Math.round(FONT_PLAIN.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_PLAIN_SIZE);
    private static final int fontHeadingHeight =
            Math.round(FONT_PLAIN.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_H1_SIZE);

    public static final PDRectangle MEDIA_BOX = new PDPage(PDRectangle.A4).getMediaBox();
    public static final float WIDTH_OF_CONTENT_COLUMN = new PDPage(PDRectangle.A4).getMediaBox().getWidth() - MARGIN * 2;

    private static final int DEFAULT_USER_SPACE_UNIT_DPI = 72;
    private static final float MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI;
    public static final float PAGE_WIDTH = 210 * MM_TO_UNITS;
    public static final float LEADING_PERCENTAGE = 1.5f;


    private PDDocument document = new PDDocument();
    private ArrayList<PDPage> completedPages;
    private PDPage currentPage = new PDPage(PDRectangle.A4);
    private ArrayList<PDPageContentStream> completedStreams;
    private PDPageContentStream currentStream;
    private float y;

    public PdfGenerator() throws IOException {
        this.currentStream = new PDPageContentStream(document, currentPage);
        this.y = calculateStartY();
    }

    public byte[] finish() throws IOException {

        // Add current page to document
        this.document.addPage(this.currentPage);

        // Close remaining streams
        this.currentStream.close();

        // save document to byte array output stream and return byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.document.save(baos);
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

    public void addHeading(String heading, String navn, String fnr) throws IOException {
        this.addCenteredH1Bold(heading);
        this.addCenteredH4Bold(navn);
        this.addCenteredH4Bold(fnr);
        this.addDividerLine();
    }

    public void addBlankLine(){
        this.y -= 20;
    }


    public static float calculateStartY() {
        return MEDIA_BOX.getUpperRightY() - MARGIN;
    }

    public void skrivTekst(String text) throws IOException {
        this.addParagraph(text, FONT_PLAIN, FONT_PLAIN_SIZE, MARGIN);
    }

    public void skrivTekstKursiv(String text) throws IOException {
        this.addParagraph(text, FONT_KURSIV, FONT_PLAIN_SIZE, MARGIN);
    }

    public void skrivTekstMedInnrykk(String text, int innrykk) throws IOException {
        this.addParagraph(text, FONT_PLAIN, FONT_PLAIN_SIZE, innrykk);
    }

    public void skrivTekstBold(String tekst) throws IOException {
        this.addParagraph(tekst, FONT_BOLD, FONT_PLAIN_SIZE, MARGIN);
    }

    public void skrivH4Bold(String tekst) throws IOException {
        this.addParagraph(tekst, FONT_BOLD, FONT_PLAIN_SIZE, MARGIN);
    }


//    public static float addBulletPoint(String line, PDPageContentStream cos, float startY) throws IOException {
//        return addLineOfRegularText("\u2022 " + line, cos, startY);
//    }

//    public static float addBulletList(List<String> lines, PDPageContentStream cos, float startY) throws IOException {
//        float yTotal = 0;
//        for (String line : lines) {
//            yTotal += addBulletPoint(line, cos, startY - yTotal);
//        }
//        return yTotal;
//    }

    public void addCenteredH1Bold(String heading) throws IOException {
        addCenteredParagraph(heading, FONT_BOLD, FONT_H1_SIZE, LEADING_PERCENTAGE);
    }

    public void addCenteredH4Bold(String heading) throws IOException {
        addCenteredParagraph(heading, FONT_BOLD, FONT_H4_SIZE, LEADING_PERCENTAGE);
    }


    public void addDividerLine() throws IOException {
        this.currentStream.setLineWidth(1);
        this.currentStream.moveTo(MARGIN, this.y);
        this.currentStream.lineTo(MEDIA_BOX.getWidth() - MARGIN, this.y);
        this.currentStream.closeAndStroke();
    }


    public void addParagraph(
            String text,
            PDFont font,
            float fontSize,
            int margin
    ) throws IOException {

        boolean justify = false;

        List<String> lines = parseLines(text, font, fontSize);
        this.currentStream.setFont(font, fontSize);
        this.currentStream.beginText();
        this.currentStream.newLineAtOffset(margin, this.y);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            float charSpacing = 0;
            if (justify) {
                if (line.length() > 1) {
                    float size = fontSize * font.getStringWidth(line) / 1000;
                    float free = WIDTH_OF_CONTENT_COLUMN - size;
                    if (free > 0 && !lines.get(lines.size() - 1).equals(line)) {
                        charSpacing = free / (line.length() - 1);
                    }
                }
            }
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
            PDFont font,
            float fontSize,
            float leadingPercentage
    ) throws IOException {
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

    private static List<String> parseLines(String text, PDFont font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = fontSize * font.getStringWidth(subString) / 1000;
            if (size > PdfGenerator.WIDTH_OF_CONTENT_COLUMN) {
                if (lastSpace < 0) {
                    lastSpace = spaceIndex;
                }
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }

}
package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

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
    private static final float MM_TO_UNITS = 1/(10*2.54f)*DEFAULT_USER_SPACE_UNIT_DPI;
    public static final float PAGE_WIDTH = 210*MM_TO_UNITS;
    public static final float LEADING_PERCENTAGE = 1.5f;

    public PDPage newPage() {
        return new PDPage(PDRectangle.A4);
    }

    public static float calculateStartY() {
        return MEDIA_BOX.getUpperRightY() - MARGIN;
    }

    public float addLineOfRegularText(String line, PDPageContentStream cos, float startY) throws IOException {
        cos.beginText();
        cos.setFont(FONT_PLAIN, FONT_PLAIN_SIZE);
        cos.moveTextPositionByAmount(MARGIN, startY);
        cos.drawString(line);
        cos.endText();
        return fontPLainHeight;
    }

    public float addLinesOfRegularText(List<String> lines, PDPageContentStream cos, float startY) throws IOException {
        float yTotal = 0;
        for (String line : lines) {
            yTotal += addLineOfRegularText(line, cos, startY - yTotal);
        }
        return yTotal;
    }

    public float addBulletPoint(String line, PDPageContentStream cos, float startY) throws IOException {
        return addLineOfRegularText("\u2022 " + line, cos, startY);
    }

    public float addBulletList(List<String> lines, PDPageContentStream cos, float startY) throws IOException {
        float yTotal = 0;
        for (String line : lines) {
            yTotal += addBulletPoint(line, cos, startY - yTotal);
        }
        return yTotal;
    }

    public float addCenteredH1Bold(PDPageContentStream cos, float startY, String heading) throws IOException {
        final float y = addCenteredParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, startY, heading, FONT_BOLD, FONT_H1_SIZE, LEADING_PERCENTAGE);
        return y;
    }

    public float addCenteredH4Bold(PDPageContentStream cos, float startY, String heading) throws IOException {
        final float y = addCenteredParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, startY, heading, FONT_BOLD, FONT_H4_SIZE, LEADING_PERCENTAGE);
        return y;
    }


    public float addLeftHeading(String heading, PDPageContentStream cos, float startY) throws IOException {
        cos.beginText();
        cos.setFont(FONT_BOLD, FONT_H1_SIZE);
        float startX = MARGIN;
        cos.moveTextPositionByAmount(startX, startY);
        cos.drawString(heading);
        cos.endText();
        return fontHeadingHeight;
    }

    public float addDividerLine(PDPageContentStream cos, float startY) throws IOException {
        cos.setLineWidth(1);
        cos.moveTo(MARGIN, startY);
        cos.lineTo(MEDIA_BOX.getWidth() - MARGIN, startY);
        cos.closeAndStroke();
        return 20;
    }


    public static float addParagraph(
            PDPageContentStream contentStream,
            float y,
            String text,
            PDFont font,
            float fontSize, int margin
    ) throws IOException {

        boolean justify = false;

        List<String> lines = parseLines(text,WIDTH_OF_CONTENT_COLUMN , font, fontSize);
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(margin, y);

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
            contentStream.setCharacterSpacing(charSpacing);
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -LEADING_PERCENTAGE * fontSize);
        }
        contentStream.endText();

        return lines.size() * fontSize * LEADING_PERCENTAGE;
    }

    public static float addCenteredParagraph(
            PDPageContentStream contentStream,
            float width,
            float sy,
            String heading,
            PDFont font,
            float fontSize,
            float leadingPercentage
    ) throws IOException {
        List<String> lines = parseLines(heading, width, font, fontSize);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);

        float prevX = 0;

        for (int i = 0; i < lines.size(); i++) {

            if (i == 0){
                float lineWidth = font.getStringWidth(lines.get(i)) / 1000 * fontSize;
                float startX = (MEDIA_BOX.getWidth() - lineWidth) / 2;
                contentStream.newLineAtOffset(startX, sy);
                prevX = startX;
            } else {
                float lineWidth = font.getStringWidth(lines.get(i)) / 1000 * fontSize;
                float startX = (MEDIA_BOX.getWidth() - lineWidth) / 2;
                contentStream.newLineAtOffset(startX-prevX, -leadingPercentage * fontSize);
                prevX = startX;
            }

            contentStream.showText(lines.get(i));

        }
        contentStream.endText();

        return lines.size()*fontSize;
    }

    private static List<String> parseLines(String text, float width, PDFont font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = fontSize * font.getStringWidth(subString) / 1000;
            if (size > width) {
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
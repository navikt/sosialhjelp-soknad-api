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

    public static final PDFont FONT_PLAIN = PDType1Font.HELVETICA;
    public static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;

    public static final int FONT_PLAIN_SIZE = 12;
    public static final int FONT_HEADING_SIZE = 16;
    private static final int fontPLainHeight =
            Math.round(FONT_PLAIN.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_PLAIN_SIZE);
    private static final int fontHeadingHeight =
            Math.round(FONT_PLAIN.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * FONT_HEADING_SIZE);

    public static final PDRectangle MEDIA_BOX = new PDPage(PDRectangle.A4).getMediaBox();

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

    public float addCenteredHeading(String heading, PDPageContentStream cos, float startY) throws IOException {
        final float y = addCenteredParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, startY, heading, FONT_BOLD, FONT_HEADING_SIZE, LEADING_PERCENTAGE);
        return y;
    }

    public float addCenteredHeadings(List<String> headings, PDPageContentStream cos, float startY) throws IOException {
        float yTotal = 0;
        for (String heading : headings) {
            yTotal += addCenteredHeading(heading, cos, startY - yTotal);
        }
        return yTotal;
    }

    public float addLeftHeading(String heading, PDPageContentStream cos, float startY) throws IOException {
        cos.beginText();
        cos.setFont(FONT_BOLD, FONT_HEADING_SIZE);
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

    public float addBlankLine() {
        return 20;
    }

    public static float addParagraph(
            PDPageContentStream contentStream,
            float width,
            float sx,
            float sy,
            String text,
            PDFont font,
            float fontSize,
            float leadingPercentage
    ) throws IOException {
        return addParagraph(contentStream, width, sx, sy, text, font, fontSize, leadingPercentage, false);
    }

    public static float addParagraph(
            PDPageContentStream contentStream,
            float width,
            float sx,
            float sy,
            String text,
            PDFont font,
            float fontSize,
            float leadingPercentage,
            boolean justify
    ) throws IOException {
        List<String> lines = parseLines(text, width, font, fontSize);
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(sx, sy);
        for (String line : lines) {
            float charSpacing = 0;
            if (justify) {
                if (line.length() > 1) {
                    float size = fontSize * font.getStringWidth(line) / 1000;
                    float free = width - size;
                    if (free > 0 && !lines.get(lines.size() - 1).equals(line)) {
                        charSpacing = free / (line.length() - 1);
                    }
                }
            }
            contentStream.setCharacterSpacing(charSpacing);
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, leadingPercentage * fontSize);
        }

        return lines.size() * fontSize;
    }

//    cos.beginText();
//    cos.setFont(fontBold, fontHeadingSize);
//    float titleWidth = fontBold.getStringWidth(heading) / 1000 * fontHeadingSize;
//    float startX = (mediaBox.getWidth() - titleWidth) / 2;
//    cos.moveTextPositionByAmount(startX, startY);
//    cos.drawString(heading);
//    cos.endText();
//    return fontHeadingHeight;

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
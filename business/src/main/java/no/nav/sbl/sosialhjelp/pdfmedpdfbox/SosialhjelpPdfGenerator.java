package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import sun.security.ssl.Debug;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.*;

@Component
public class SosialhjelpPdfGenerator {

    private PdfGenerator pdfGen;

    private static final float FONT_SIZE = 12;
    public static final float PAGE_WIDTH = 400;
    public static final float PAGE_HEIGHT = 500;

    @Inject
    private NavMessageSource navMessageSource;

    @Inject
    public SosialhjelpPdfGenerator() {
        pdfGen = new PdfGenerator();
    }

    public byte[] generate(JsonInternalSoknad jsonInternalSoknad) {
        try {
            PDDocument doc = new PDDocument();
            final PDPage page1 = pdfGen.newPage();
            PDPageContentStream cos1 = new PDPageContentStream(doc, page1);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();


            float y = PdfGenerator.calculateStartY();

            // write stuff
            y -= header(doc, cos1, y);

            PDRectangle mediaBox = page1.getMediaBox();
            float marginY = 80;
            float marginX = 60;
            float width = mediaBox.getWidth() - 2 * marginX;
            float startX = mediaBox.getLowerLeftX() + marginX;
            float startY = mediaBox.getUpperRightY() - marginY;


            cos1.beginText();
            y -= addParagraph(cos1, width, startX, startY, "Første vanlige linje", PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, PdfGenerator.LEADING_PERCENTAGE);
            cos1.endText();
            y -= leggTilPersonalia(doc, cos1, y, jsonInternalSoknad.getSoknad().getData().getPersonalia());



            final PDPage page2 = pdfGen.newPage();

            doc.addPage(page1);
            doc.addPage(page2);

            cos1.close();
            doc.save(baos);
            byte[] pdf;
            pdf = baos.toByteArray();
            return pdf;

        } catch (IOException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }

    private float leggTilPersonalia(PDDocument doc, PDPageContentStream cos, float y, JsonPersonalia jsonPersonalia) throws IOException {

//        final Properties tekstbundle = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));
//        final String personaliabolkTittel = tekstbundle.getProperty("personaliabolk.tittel");


        if (jsonPersonalia.getNavn() != null) {


            final Debug debug = new Debug();
            debug.println("y = " + y);

            cos.beginText();
            y -= addParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, MARGIN, y, "Navn på person", FONT_BOLD, FONT_SIZE, LEADING_PERCENTAGE);
            debug.println("y = " + y);
            y -= addParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, MARGIN, -y, jsonPersonalia.getNavn().getFornavn() + " " + jsonPersonalia.getNavn().getEtternavn(), FONT_PLAIN, FONT_SIZE, LEADING_PERCENTAGE);
            debug.println("y = " + y);
            y -= addParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, MARGIN, -y, "Telefonnummer", FONT_BOLD, FONT_SIZE, LEADING_PERCENTAGE);
            debug.println("y = " + y);
            y -= addParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, MARGIN, -y, "asdf " + jsonPersonalia.getTelefonnummer().getVerdi(), FONT_PLAIN, FONT_SIZE, LEADING_PERCENTAGE);
            debug.println("y = " + y);
            y -= addParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, MARGIN, -y, "Bankkontonummer", FONT_BOLD, FONT_SIZE, LEADING_PERCENTAGE);
            debug.println("y = " + y);
            y -= addParagraph(cos, MEDIA_BOX.getWidth() - 2 * MARGIN, MARGIN, -y, "asdf " + jsonPersonalia.getKontonummer().getVerdi(), FONT_PLAIN, FONT_SIZE, LEADING_PERCENTAGE);
            debug.println("y = " + y);
            cos.endText();
        }


        return y;
    }

    private float header(PDDocument doc, PDPageContentStream cos, float y) throws IOException {
        float startY = y;
        y -= pdfGen.addCenteredHeading("Heart of the swarm - med en veldig veldig veldig veldig veldig glhf glhf glhf glhf lang tittel.", cos, y);
        y -= pdfGen.addDividerLine(cos, y);
        return startY - y;
    }

    public byte[] pracGenerate() {

        try (final PDDocument doc = new PDDocument()) {

            final PdfGenerator pdfGen = new PdfGenerator();

            PDPage page = pdfGen.newPage();
            PDPage page2 = pdfGen.newPage();
            doc.addPage(page);
            doc.addPage(page2);
            PDPageContentStream contentStreamPage1 = new PDPageContentStream(doc, page);
            PDPageContentStream contentStreamPage2 = new PDPageContentStream(doc, page2);


            PDRectangle mediaBox = page.getMediaBox();
            float marginY = 80;
            float marginX = 60;
            float width = mediaBox.getWidth() - 2 * marginX;
            float startX = mediaBox.getLowerLeftX() + marginX;
            float startY = mediaBox.getUpperRightY() - marginY;

            String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat" +
                    " non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";


            contentStreamPage1.lineTo(0, 0);
            contentStreamPage1.closeAndStroke();
            contentStreamPage1.addLine(5, 5, PAGE_WIDTH, PAGE_HEIGHT);

            contentStreamPage1.beginText();
            addParagraph(contentStreamPage1, width, startX, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, PdfGenerator.LEADING_PERCENTAGE, true);
            addParagraph(contentStreamPage1, width, startX, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, PdfGenerator.LEADING_PERCENTAGE);
            addParagraph(contentStreamPage1, width, startX, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, PdfGenerator.LEADING_PERCENTAGE, false);
            contentStreamPage1.endText();
            contentStreamPage1.close();

            contentStreamPage2.beginText();
            addParagraph(contentStreamPage1, width, startX, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, PdfGenerator.LEADING_PERCENTAGE, true);
            contentStreamPage2.endText();
            contentStreamPage2.close();


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            System.err.println("Exception while trying to create pdf document - " + e);
        }

        return null;

    }


}

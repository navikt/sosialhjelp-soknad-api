package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class SosialhjelpPdfGenerator {

    private PdfGenerator pdfGen;

    @Inject
    public SosialhjelpPdfGenerator() {
        pdfGen = new PdfGenerator();
    }

    public byte[] generate(){

        byte[] pdf;
        final PDPage page = pdfGen.newPage();
        try (
                PDDocument doc = new PDDocument();
                PDPageContentStream cos = new PDPageContentStream(doc, page);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ){
            float y = PdfGenerator.calculateStartY();

            // write stuff
            header(doc, cos, y);
            pdfGen.addBlankLine();

            doc.addPage(page);
            cos.close();
            doc.save(baos);
            pdf = baos.toByteArray();
            return pdf;


        } catch (IOException | COSVisitorException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }

    private float header(PDDocument doc, PDPageContentStream cos, float y) throws IOException {
        float startY = y;
        y -= pdfGen.addCenteredHeading("My life, for Aiur.", cos, y);
        y -= pdfGen.addDividerLine(cos, y);
        return startY - y;
    }
}

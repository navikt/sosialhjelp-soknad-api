package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

@Component
public class SosialhjelpPdfGenerator {

    @Inject
    private NavMessageSource navMessageSource;


    private PdfGenerator pdfGen;

    @Inject
    public SosialhjelpPdfGenerator() {
        pdfGen = new PdfGenerator();
    }

    public byte[] generate(JsonInternalSoknad jsonInternalSoknad){

        byte[] pdf;
        final PDPage page = pdfGen.newPage();
        try (
                PDDocument doc = new PDDocument();
                PDPageContentStream cos = new PDPageContentStream(doc, page);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ){
            float y = PdfGenerator.calculateStartY();

            // write stuff
            y -= header(doc, cos, y);
            y -= leggTilPersonalia(doc, cos, y, jsonInternalSoknad.getSoknad().getData().getPersonalia());




            final PDPage page2 = pdfGen.newPage();



            doc.addPage(page);
            doc.addPage(page2);


            cos.close();
            doc.save(baos);
            pdf = baos.toByteArray();
            return pdf;

        } catch (IOException | COSVisitorException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }

    private float leggTilPersonalia(PDDocument doc, PDPageContentStream cos, float y, JsonPersonalia jsonPersonalia) throws IOException {

//        final Properties tekstbundle = navMessageSource.getBundleFor("sendsoknad", new Locale("nb", "NO"));
//        final String personaliabolkTittel = tekstbundle.getProperty("personaliabolk.tittel");
        float startY = y;

        cos.beginText();
        cos.setFont(fontPlain, fontPLainSize);
        cos.moveTextPositionByAmount(margin, startY);
        cos.drawString(line);
        cos.endText();





        y -= pdfGen.addLineOfRegularText("Personalia", cos, y);



        return startY - y;
    }

    private float header(PDDocument doc, PDPageContentStream cos, float y) throws IOException {
        float startY = y;
        y -= pdfGen.addCenteredHeading("Heart of the swarm", cos, y);
        y -= pdfGen.addDividerLine(cos, y);
        return startY - y;
    }
}

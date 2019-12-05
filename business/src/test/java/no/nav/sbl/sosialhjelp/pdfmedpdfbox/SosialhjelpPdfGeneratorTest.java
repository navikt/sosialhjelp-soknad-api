package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sun.security.ssl.Debug;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SosialhjelpPdfGeneratorTest {

    @Mock
    NavMessageSource messageSource;

    @InjectMocks
    SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    @Before
    public void setUp() {
        Properties properties = new Properties();
        properties.setProperty("personaliabolk.tittel", "personalia");
        when(messageSource.getBundleFor(any(), any())).thenReturn(properties);
    }

    @Test
    public void testGenerate() {
        //SosialhjelpPdfGenerator sosialhjelpPdfGenerator =  new SosialhjelpPdfGenerator();

        final Properties bundle = new NavMessageSource().getBundleFor("sendsoknad", new Locale("nb", "NO"));


        final JsonData data = new JsonData()
                .withPersonalia(
                        new JsonPersonalia()
                                .withNavn(
                                        new JsonSokernavn()
                                                .withFornavn("Han")
                                                .withEtternavn("Solo")
                                )
                                .withStatsborgerskap(
                                        new JsonStatsborgerskap().withVerdi("Norsk")
                                )
                                .withOppholdsadresse(
                                        new JsonAdresse()
                                                .withType(JsonAdresse.Type.GATEADRESSE)
                                                .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)

                                )
                                .withFolkeregistrertAdresse(
                                        new JsonAdresse()
                                                .withAdditionalProperty("landkode", "NOR")
                                                .withAdditionalProperty("kommunenummer", "0701")
                                                .withAdditionalProperty("gatenavn", "SANNERGATA")
                                                .withAdditionalProperty("husnummer", "13")
                                                .withType(JsonAdresse.Type.GATEADRESSE)
                                )
                                .withTelefonnummer(new JsonTelefonnummer().withVerdi("99887766").withKilde(JsonKilde.BRUKER))
                                .withKontonummer(new JsonKontonummer().withKilde(JsonKilde.SYSTEM).withVerdi("12345678903"))
                )
                .withBegrunnelse(
                        new JsonBegrunnelse()
                                .withHvaSokesOm("Jeg søker om penger til gaming.")
                                .withHvorforSoke("Fordi jeg liker gaming")
                );

        final JsonSoknad jsonSoknad = new JsonSoknad().withData(data);
        final JsonInternalSoknad jsonInternalSoknad = new JsonInternalSoknad().withSoknad(jsonSoknad);


        byte[] bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad);

        try {
            FileOutputStream out = new FileOutputStream("../temp/starcraft.pdf");
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerateTest() {

        try {
            final byte[] bytes = sosialhjelpPdfGenerator.pracGenerate();
            FileOutputStream out = new FileOutputStream("../temp/snowboard.pdf");
            out.write(bytes);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void trening() {
        try (
                final PDDocument doc = new PDDocument()
        ) {

            final float A4_PAGE_WIDTH = new PDPage(PDRectangle.A4).getMediaBox().getWidth(); // 595.27563
            final float A4_PAGE_HEIGHT = new PDPage(PDRectangle.A4).getMediaBox().getHeight(); // 841.8898


            final PDPage page1 = new PDPage(PDRectangle.A4);
            final PDPage page2 = new PDPage(PDRectangle.A4);
            doc.addPage(page1);
            doc.addPage(page2);
            final PDPageContentStream content1 = new PDPageContentStream(doc, page1);
            final PDPageContentStream content2 = new PDPageContentStream(doc, page2);

            final PDRectangle mediaBox1 = page1.getMediaBox();
            final float width = mediaBox1.getWidth();
            final float height = mediaBox1.getHeight();

            // lag ting her:

            content1.beginText();
            content1.setFont(PDType1Font.HELVETICA, 26);
            content1.newLineAtOffset(0, A4_PAGE_HEIGHT - 26);
            content1.showText("My life, for Aiur.");
            content1.setLeading(26 * 1.5f);
            content1.newLine();
            content1.showText("Kommer dette på en ny linje?");
            content1.endText();


            //


            content1.close();
            content2.close();

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            FileOutputStream out = new FileOutputStream("../temp/spike.pdf");
            out.write(baos.toByteArray());
            out.close();

            final Debug debug = new Debug();
            debug.println("width = " + width);
            debug.println("height = " + height);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void exampleFromApache() {
        // Create a new document with an empty page.
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Adobe Acrobat uses Helvetica as a default font and
            // stores that under the name '/Helv' in the resources dictionary
            PDFont font = PDType1Font.HELVETICA;
            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Helv"), font);

            // Add a new AcroForm and add that to the document
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);

            // Add and set the resources and default appearance at the form level
            acroForm.setDefaultResources(resources);

            // Acrobat sets the font size on the form level to be
            // auto sized as default. This is done by setting the font size to '0'
            String defaultAppearanceString = "/Helv 0 Tf 0 g";
            acroForm.setDefaultAppearance(defaultAppearanceString);

            // Add a form field to the form.
            PDTextField textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleField");

            // Acrobat sets the font size to 12 as default
            // This is done by setting the font size to '12' on the
            // field level.
            // The text color is set to blue in this example.
            // To use black, replace "0 0 1 rg" with "0 0 0 rg" or "0 g".
            defaultAppearanceString = "/Helv 12 Tf 0 0 1 rg";
            textBox.setDefaultAppearance(defaultAppearanceString);

            // add the field to the acroform
            acroForm.getFields().add(textBox);

            // Specify the widget annotation associated with the field
            PDAnnotationWidget widget = textBox.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(50, 750, 200, 50);
            widget.setRectangle(rect);
            widget.setPage(page);

            // set green border and yellow background
            // if you prefer defaults, delete this code block
            PDAppearanceCharacteristicsDictionary fieldAppearance
                    = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
            fieldAppearance.setBorderColour(new PDColor(new float[]{0, 1, 0}, PDDeviceRGB.INSTANCE));
            fieldAppearance.setBackground(new PDColor(new float[]{1, 1, 0}, PDDeviceRGB.INSTANCE));
            widget.setAppearanceCharacteristics(fieldAppearance);

            // make sure the widget annotation is visible on screen and paper
            widget.setPrinted(true);

            // Add the widget annotation to the page
            page.getAnnotations().add(widget);

            // set the field value
            textBox.setValue("Sample field content");

            // put some text near the field
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 15);
                cs.newLineAtOffset(50, 810);
                cs.showText("Field:");
                cs.endText();
            }

            document.save("../temp/SimpleForm.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

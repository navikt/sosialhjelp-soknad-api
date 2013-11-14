package no.nav.sbl.dialogarena.print;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static no.nav.sbl.dialogarena.print.PDFGenerator.createPDFFromImage;
import static no.nav.sbl.dialogarena.print.PDFGenerator.renderHTMLToImage;
import static no.nav.sbl.dialogarena.print.PDFGenerator.renderHTMLToPDF;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


public class PDFGeneratorTest {

    private String path;
    private String pdfPath;
    private String pdfPath1;
    private String imagePath;
    private String stylePath;
    private String pdfPath2;
    private String xslPath;
    private static final boolean RUN_LOCAL = true;

    @Before
    public void setUp() throws Exception {
        String page = "/html/TestSide.html";
        String css = "/html/style.css";
        String xsl = "/html/people.xsl";
        stylePath = getClass().getResource(css).getPath();
        path = getClass().getResource(page).getPath();
        pdfPath = RUN_LOCAL ? "c:/test/myPdf.pdf" : "";
        pdfPath1 = RUN_LOCAL ? "c:/test/myPdf1.pdf" : "";
        pdfPath2 = RUN_LOCAL ? "c:/test/myPdf2.pdf" : "";
        imagePath = "c:/test/myPng.png";

        xslPath = getClass().getResource(xsl).getPath();
        xslPath = xslPath.substring(1);
    }

    @Test
    public void testRenderHTMLToImage() throws Exception {
        BufferedImage image = renderHTMLToImage(path);

        saveImage(image, "c:/test/myPng.png");
        assertThat(image, is(notNullValue()));
        assertThat(image.getData().getDataBuffer().getSize(), is(greaterThan(1000)));
    }

    @Test
    public void testRenderHTMLToPDF() throws Exception {
        boolean rendered = renderHTMLToPDF(path, pdfPath1, null);

        File pdf = new File(pdfPath1);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Ignore
    @Test
    public void testRenderXMLDocumentToPDF() throws Exception {
        Document doc = XMLGenerator.createDocument("");
        String nyXML = "c:/test/minXMLMedXSL.xml";
        // XMLGenerator.createXMLFile(nyXML, doc);

        XMLGenerator.addXSLToDocument(xslPath, nyXML, doc);
//        File xml = new File(nyXML);
//        assertThat(xml.exists(), is(true));

        boolean rendered = renderHTMLToPDF(nyXML, pdfPath2, null);

        File pdf = new File(pdfPath2);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Test
    public void testRenderDocumentToHTMLToPdf() throws Exception {
        long start = new Date().getTime();
        Document doc = XMLGenerator.createDocument(stylePath);
        String nyHTML = "c:/test/minNyeHTML1.html";
        String nyPDF = "c:/test/minNyePDF1.pdf";

        XMLGenerator.createHTMLFile(nyHTML, doc, xslPath);

        boolean rendered = renderHTMLToPDF(nyHTML, nyPDF, null);
        long stop = new Date().getTime();
        long diff = stop - start;
        System.out.println("testRenderDocumentToHTMLToPdf diff = " + diff);

        File pdf = new File(nyPDF);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Ignore
    @Test
    public void testRenderXMLFileToHtmlToPDF() throws Exception {
        long start = new Date().getTime();
        Document doc = XMLGenerator.createDocument("");
        String nyXML = "c:/test/minNyeXML.xml";
        String nyHTML = "c:/test/minNyeHTML.html";
        String nyPDF = "c:/test/minNyePDF.pdf";

        XMLGenerator.createXMLFile(nyXML, doc);
        XMLGenerator.createXMLFile(xslPath, nyXML, nyHTML);

        //XMLGenerator.addXSLToDocument(xslPath, nyXML, doc);
//        File xml = new File(nyXML);
//        assertThat(xml.exists(), is(true));

        boolean rendered = renderHTMLToPDF(nyHTML, nyPDF, null);
        long stop = new Date().getTime();
        long diff = stop - start;
        System.out.println("testRenderXMLFileToHtmlToPDF diff = " + diff);

        File pdf = new File(nyPDF);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Test
    public void testRenderWicketHTMLToImage() throws Exception {
        String wicketPath = getClass().getResource("/html/WicketPageSimple.html").getPath();
        File f = new File(wicketPath);
        if (!f.exists()) {
            System.out.println("FOKK!");
        }
        BufferedImage image = renderHTMLToImage(wicketPath);

        saveImage(image, "c:/test/myPng_wicket.png");
        assertThat(image, is(notNullValue()));
        assertThat(image.getData().getDataBuffer().getSize(), is(greaterThan(1000)));
    }

    @Test
    public void testImageToPdf() throws Exception {
        BufferedImage image = renderHTMLToImage(path);
        String imgPath = "c:/test/myPng1.png";
        saveImage(image, imgPath);

        int marginLeft = 20;
        int marginTop = 40;
        createPDFFromImage(image, pdfPath, marginLeft, marginTop);

        if (RUN_LOCAL) {
            File pdf = new File(pdfPath);
            assertThat(pdf, is(notNullValue()));
            assertThat(pdf.canRead(), is(equalTo(true)));
            assertThat(pdf.isFile(), is(equalTo(true)));
        }
    }

    private void saveImage(BufferedImage image, String imgPath) throws IOException {
        if (image != null && RUN_LOCAL) {
            ImageIO.write(image, "png", new File(imgPath));
        }
    }
}

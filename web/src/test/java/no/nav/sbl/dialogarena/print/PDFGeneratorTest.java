package no.nav.sbl.dialogarena.print;


import no.nav.sbl.dialogarena.print.helper.XMLTestData;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static no.nav.sbl.dialogarena.print.PDFGenerator.createPDFFromHTML;
import static no.nav.sbl.dialogarena.print.PDFGenerator.createPDFFromImage;
import static no.nav.sbl.dialogarena.print.PDFGenerator.renderHTMLToImage;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@Ignore
public class PDFGeneratorTest {

    private String testHtmlPath;
    private String pdfPath1;
    private String cssFile;
    private String xslPath;
    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        String page = "/html/TestSide.html";
        String css = "/html/style.css";
        String xsl = "/html/people.xsl";
        testHtmlPath = getClass().getResource(page).getPath();
        pdfPath1 = "c:/test/myPdf1.pdf";

        xslPath = getClass().getResource(xsl).getPath();
        xslPath = xslPath.substring(1);
        cssFile = getClass().getResource(css).getPath();
        cssFile = cssFile.substring(1);
        baseUrl = getClass().getResource("/html/").getPath();
        baseUrl = baseUrl.substring(1);
    }

    @Test
    public void testRenderHTMLToImage() throws Exception {
        BufferedImage image = renderHTMLToImage(testHtmlPath);

        saveImage(image, "c:/test/myPng.png");
        assertThat(image, is(notNullValue()));
        assertThat(image.getData().getDataBuffer().getSize(), is(greaterThan(1000)));
    }

    @Test
    public void testRenderHTMLToPDF() throws Exception {
        boolean rendered = createPDFFromHTML(testHtmlPath, pdfPath1);

        File pdf = new File(pdfPath1);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Test
    public void testRenderDocumentToHTMLToPdf() throws Exception {
        long start = new Date().getTime();
        Document doc = XMLTestData.createDocument();
        String nyHTML = "c:/test/minNyeHTML1.html";
        String nyPDF = "c:/test/minNyePDF1.pdf";

        XMLGenerator.transformToHTML(nyHTML, doc, xslPath);

        boolean rendered = createPDFFromHTML(nyHTML, nyPDF);
        long stop = new Date().getTime();
        long diff = stop - start;
        System.out.println("testRenderDocumentToHTMLToPdf diff = " + diff);

        File pdf = new File(nyPDF);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Test
    public void testHTMLToPdf() throws Exception {
        long start = new Date().getTime();
        Document doc = XMLTestData.createDocument();
        String nyHTML = "c:/test/minNyeHTML1.html";
        String nyPDF = "c:/test/minNyePDF1.pdf";

        XMLGenerator.transformToHTML(nyHTML, doc, xslPath);

        boolean rendered = createPDFFromHTML(nyHTML, nyPDF);
        long stop = new Date().getTime();
        long diff = stop - start;
        System.out.println("testRenderDocumentToHTMLToPdf diff = " + diff);

        File pdf = new File(nyPDF);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Test
    public void testDocumentToPdf() throws Exception {
        long start = new Date().getTime();
        Document doc = XMLTestData.createDocument();
        String nyHTML = "c:/test/testItHtml.html";
        String nyPDF = "c:/test/minNyePDF1.pdf";

        XMLGenerator.transformToHTML(nyHTML, doc, xslPath);

        boolean rendered = createPDFFromHTML(nyHTML, nyPDF);
        long stop = new Date().getTime();
        long diff = stop - start;
        System.out.println("testRenderDocumentToHTMLToPdf diff = " + diff);

        File pdf = new File(nyPDF);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    @Test
    public void testDocumentToPdf1() throws Exception {
        long start = new Date().getTime();
        Document doc = XMLTestData.createDocument();
        String nyPDF = "c:/test/minNyePDF3.pdf";

        StringBuilder sb = new StringBuilder();
        String htmlHeader = getHeader(cssFile);
        String htmlFooter = getFooter();

        String bodyHtml = XMLGenerator.transformToHTML(doc, xslPath);
        String nyHtml = sb.append(htmlHeader).append(bodyHtml).append(htmlFooter).toString();
        System.out.println("nyHtml = " + nyHtml);

        boolean rendered = createPDFFromHTML(nyHtml, baseUrl, nyPDF);

        //boolean rendered = renderHTMLToPDF(nyHtml, nyPDF);
        long stop = new Date().getTime();
        long diff = stop - start;
        System.out.println("testRenderDocumentToHTMLToPdf diff = " + diff);

        File pdf = new File(nyPDF);
        assertThat(rendered, is(true));
        assertThat(pdf.exists(), is(true));
    }

    private String getFooter() {
        return "</body></html>";
    }

    private String getHeader(String css) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><link rel=\"stylesheet\" type=\"text/css\" href=\"" + css + "\" /></head><body>\n";
    }

    @Test
    public void testRenderWicketHTMLToImage() throws Exception {
        String wicketPath = getClass().getResource("/html/WicketPageSimple.html").getPath();
        BufferedImage image = renderHTMLToImage(wicketPath);

        saveImage(image, "c:/test/myPng_wicket.png");
        assertThat(image, is(notNullValue()));
        assertThat(image.getData().getDataBuffer().getSize(), is(greaterThan(1000)));
    }

    @Test
    public void testImageToPdf() throws Exception {
        BufferedImage image = renderHTMLToImage(testHtmlPath);
        String imgPath = "c:/test/myPng1.png";
        saveImage(image, imgPath);

        int marginLeft = 20;
        int marginTop = 40;
        createPDFFromImage(image, pdfPath1 + "_3", marginLeft, marginTop);

        File pdf = new File(pdfPath1 + "_3");
        assertThat(pdf, is(notNullValue()));
        assertThat(pdf.canRead(), is(equalTo(true)));
        assertThat(pdf.isFile(), is(equalTo(true)));
    }

    private void saveImage(BufferedImage image, String imgPath) throws IOException {
        if (image != null) {
            ImageIO.write(image, "png", new File(imgPath));
        }
    }
}

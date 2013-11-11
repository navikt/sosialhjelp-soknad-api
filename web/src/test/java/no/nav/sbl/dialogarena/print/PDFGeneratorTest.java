package no.nav.sbl.dialogarena.print;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static no.nav.sbl.dialogarena.print.PDFGenerator.createPDFFromImage;
import static no.nav.sbl.dialogarena.print.PDFGenerator.renderHTMLToImage;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


public class PDFGeneratorTest {

    private String path;
    private String pdfPath;
    private String imagePath;
    private static final boolean RUN_LOCAL = false;

    @Before
    public void setUp() throws Exception {
        String page = "/html/TestSide.html";
        path = getClass().getResource(page).getPath();
        pdfPath = "c:/dev/test/myPdf.pdf";
        imagePath = "c:/dev/test/myPng.png";
        System.out.println("path = " + path);
    }

    @Test
    public void testRenderHTMLToImage() throws Exception {
        BufferedImage image = renderHTMLToImage(path);

        saveImage(image, imagePath);
        assertThat(image, is(notNullValue()));
        assertThat(image.getData().getDataBuffer().getSize(), is(greaterThan(1000)));
    }

    @Test
    public void testImageToPdf() throws Exception {
        BufferedImage image = renderHTMLToImage(path);
        String imgPath = "c:/dev/test/myPng1.png";
        saveImage(image, imgPath);
        PDDocument pdf = createPDFFromImage(image);

        pdf.save(pdfPath);

        assertThat(pdf, is(notNullValue()));
        assertThat(pdf.getNumberOfPages(), is(greaterThan(0)));
    }

    private void saveImage(BufferedImage image, String imgPath) throws IOException {
        if(image != null && RUN_LOCAL) {
            ImageIO.write(image, "PNG", new File(imgPath));
        }
    }
}

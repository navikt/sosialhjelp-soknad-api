package no.nav.sbl.dialogarena.print;


import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static no.nav.sbl.dialogarena.print.PDFGenerator.createPDFFromImage;
import static no.nav.sbl.dialogarena.print.PDFGenerator.renderHTMLToImage;
import static org.hamcrest.Matchers.equalTo;
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
        pdfPath = RUN_LOCAL ? "c:/test/myPdf.pdf" : "";
        imagePath = "c:/test/myPng.png";
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
        String imgPath = "c:/test/myPng1.png";
        saveImage(image, imgPath);

        int marginLeft = 20;
        int marginTop = 40;
        createPDFFromImage(image, pdfPath, marginLeft, marginTop);

        if(RUN_LOCAL) {
            File pdf = new File(pdfPath);
            assertThat(pdf, is(notNullValue()));
            assertThat(pdf.canRead(), is(equalTo(true)));
            assertThat(pdf.isFile(), is(equalTo(true)));
        }
    }

    private void saveImage(BufferedImage image, String imgPath) throws IOException {
        if(image != null && RUN_LOCAL) {
            ImageIO.write(image, "PNG", new File(imgPath));
        }
    }
}

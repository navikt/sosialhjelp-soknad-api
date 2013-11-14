package no.nav.sbl.dialogarena.print;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class XMLGeneratorTest {
    String resource;
    String filePath = "c:/test/min.xml";
    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        String path = getClass().getResource("/html/people.xsl").getPath();
        baseUrl = getClass().getResource("/html/").getPath();
        //resource = path.substring(1);
        resource = "people.xsl";
    }

    @Test
    public void generateXMLFileDocument() throws Exception {
        Document document = XMLGenerator.createDocument(resource);
        XMLGenerator.createXMLFile(filePath, document);
    }

    @Test
    public void generateImageFromDocument() throws Exception {
        Document document = XMLGenerator.createDocument(resource);
        BufferedImage image = PDFGenerator.getBufferedImageFromDocument(document, baseUrl, 700, BufferedImage.TYPE_BYTE_BINARY);
        saveToPng(image, "c:/test/minDocXmlPng.png");
    }

    @Test
    public void generateImageFromXML() throws Exception {
        BufferedImage bufferedImage = PDFGenerator.createBuf(filePath, 500);
        saveToPng(bufferedImage, "c:/test/minXmlPng.png");

        bufferedImage = PDFGenerator.renderHTMLToImage(filePath );
        ImageIO.write(bufferedImage, "PNG", new File("c:/test/minXmlPng_style.png"));

    }

    private void saveToPng(BufferedImage bufferedImage, String pathname) throws IOException {
        ImageIO.write(bufferedImage, "PNG", new File(pathname));
    }

}

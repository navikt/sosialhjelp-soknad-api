package no.nav.sbl.dialogarena.print;


import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;

import static java.awt.image.BufferedImage.*;

public class PDFGenerator {


    public static BufferedImage renderHTMLToImage(String fileName) throws MalformedURLException {
        return Graphics2DRenderer.renderToImageAutoSize(fileName, 700, TYPE_BYTE_BINARY);
    }

    public static PDDocument createPDFFromImage(BufferedImage image) {
        PDDocument doc = null;
        PDPage page = new PDPage();

        try {
            doc = new PDDocument();
            doc.addPage(page);

            PDXObjectImage pdxImage = new PDJpeg(doc, image, 0.56f);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.drawImage(pdxImage, image.getWidth(), image.getHeight());
            contentStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

}

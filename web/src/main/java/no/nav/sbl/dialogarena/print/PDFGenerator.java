package no.nav.sbl.dialogarena.print;


import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static org.apache.pdfbox.pdmodel.PDPage.*;

public class PDFGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(PDFGenerator.class);

    public static BufferedImage renderHTMLToImage(String fileName) throws MalformedURLException {
        return Graphics2DRenderer.renderToImageAutoSize(fileName, 700, TYPE_BYTE_BINARY);
    }

    public static void createPDFFromImage(BufferedImage image, String filePath, float marginLeft, float marginTop) {
        PDDocument doc = null;
        PDPage page = new PDPage(PAGE_SIZE_A4);

        try {
            doc = new PDDocument();
            doc.addPage(page);

            PDXObjectImage pdxImage = new PDJpeg(doc, image);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.drawImage(pdxImage, marginLeft, page.getMediaBox().getHeight() - pdxImage.getHeight() - marginTop);
            contentStream.close();

        } catch (IOException e) {
            LOG.info("Feil under PDF-generering ", e);
        }

        if (doc != null && filePath != null && !filePath.isEmpty()) {
            try {
                doc.save(filePath);
                doc.close();
            } catch (IOException | COSVisitorException e) {
                LOG.info("Feil under lagring av pdf ", e);
            }
        }
    }

}

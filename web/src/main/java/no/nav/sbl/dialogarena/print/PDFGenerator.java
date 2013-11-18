package no.nav.sbl.dialogarena.print;


import com.lowagie.text.DocumentException;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xml.sax.InputSource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static org.apache.pdfbox.pdmodel.PDPage.PAGE_SIZE_A4;

public class PDFGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(PDFGenerator.class);

    public static BufferedImage renderHTMLToImage(String fileName) throws MalformedURLException {
        File f = new File(fileName);
        if (!f.exists()) {
            return new BufferedImage(0, 0, TYPE_BYTE_BINARY);
        }

        return Graphics2DRenderer.renderToImageAutoSize(f.toURI().toURL().toExternalForm(), 700, TYPE_BYTE_BINARY);
    }

    public static BufferedImage renderHTMLToImage(String fileName, int type) throws MalformedURLException {
        return Graphics2DRenderer.renderToImageAutoSize(fileName, 700, type);
    }

    public static boolean createPDFFromHTML(String inputHtml, String baseUrl, String outputFile) {

        boolean result = false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            ITextRenderer renderer = new ITextRenderer();

            Document doc = XMLResource.load(new InputSource(new StringReader(inputHtml))).getDocument();

            baseUrl = getURLString(baseUrl);
            renderer.setDocument(doc, baseUrl);
            renderer.layout();
            renderer.createPDF(out);

            result = true;
        } catch (IOException | DocumentException e) {
            LOG.info("Feil under lesing av XML", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.info("Feil under lesing av XML", e);
                }
            }
        }
        return result;
    }

    public static boolean createPDFFromHTML(String inputUrl, String outputFile) throws DocumentException {

        boolean result = false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);

            ITextRenderer renderer = new ITextRenderer();
            inputUrl = getURLString(inputUrl);
            Document doc = XMLResource.load(new InputSource(inputUrl)).getDocument();

            renderer.setDocument(doc, inputUrl);
            renderer.layout();
            renderer.createPDF(out);
            result = true;
        } catch (IOException e) {
            LOG.info("Feil under lesing av XML", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.info("Feil under lesing av XML", e);
                }
            }
        }
        return result;
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

    private static String getURLString(String inputUrl) throws MalformedURLException {
        String url = inputUrl;
        File f = new File(inputUrl);
        if (f.exists()) {
            url = f.toURI().toURL().toString();
        }
        return url;
    }

}

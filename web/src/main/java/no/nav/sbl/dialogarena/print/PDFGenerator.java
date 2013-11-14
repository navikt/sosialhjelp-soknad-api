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
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.DownscaleQuality;
import org.xhtmlrenderer.util.FSImageWriter;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.ScalingOptions;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import static org.apache.pdfbox.pdmodel.PDPage.PAGE_SIZE_A4;

public class PDFGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(PDFGenerator.class);

    public static BufferedImage renderHTMLToImage(String fileName) throws MalformedURLException {
        File f = new File(fileName);
        if (!f.exists()) { return new BufferedImage(0,0,TYPE_BYTE_BINARY); }

        return Graphics2DRenderer.renderToImageAutoSize(f.toURI().toURL().toExternalForm(), 700, TYPE_BYTE_BINARY);
    }

    public static boolean renderHTMLToPDF(String inputFile, String outputFile, Document document) throws IOException, DocumentException {
        String inputUrl = null;
        if (inputFile != null) {
            File f = new File(inputFile);
            if (!f.exists()) {
                System.out.println("FOKK! fant ikke html-inputfil: " + inputFile);
                return false;
            }

            inputUrl = f.toURI().toURL().toString();
        }

        return createPDF(inputUrl, outputFile, document);
    }

    private static class ResourceLoaderUserAgent extends ITextUserAgent {

        public ResourceLoaderUserAgent(ITextOutputDevice outputDevice) {
            super(outputDevice);
        }

        protected InputStream resolveAndOpenStream (String url) {
            return super.resolveAndOpenStream(url);
        }
    }

    public static boolean createPDF(String inputUrl, String outputFile, Document document) throws  DocumentException {

        boolean result = false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);

            ITextRenderer renderer = getiTextRenderer();

            Document doc = document;
            if(doc == null) {
                doc = XMLResource.load(new InputSource(inputUrl)).getDocument();
            }

            renderer.setDocument(doc, inputUrl);
            renderer.layout();
            renderer.createPDF(out);
            out.close();
            out = null;

            result = true;

        } catch (IOException e) {
            LOG.info("Feil under lesing av XML", e);
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.info("Feil under lesing av XML", e);
                }
            }
        }

        return result;
    }

    private static ITextRenderer getiTextRenderer() {
        ITextRenderer renderer = new ITextRenderer();
        ResourceLoaderUserAgent agent = new ResourceLoaderUserAgent(renderer.getOutputDevice());
        agent.setSharedContext(renderer.getSharedContext());
        renderer.getSharedContext().setUserAgentCallback(agent);
        return renderer;
    }

    public static BufferedImage renderHTMLToImage(String fileName, int type) throws MalformedURLException {
        return Graphics2DRenderer.renderToImageAutoSize(fileName, 700, type);
    }



    public static BufferedImage getBufferedImageFromDocument(Document document, String baseUrl, int width, int bufferedImageType) {
        Graphics2DRenderer g2r = new Graphics2DRenderer();
        g2r.setDocument(document, baseUrl);
        return renderToImage(width, bufferedImageType, g2r);
    }

    public static BufferedImage getBufferedImageFromDocument(String path, String baseUrl, int width, int bufferedImageType) {
        XMLReader reader;
        Graphics2DRenderer g2r = new Graphics2DRenderer();
        g2r.setDocument(path);
        return renderToImage(width, bufferedImageType, g2r);
    }

    private static BufferedImage renderToImage(int width, int bufferedImageType, Graphics2DRenderer g2r) {
        Dimension dim = new Dimension(width, 1000);

        // do layout with temp buffer
        BufferedImage buff = new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(), bufferedImageType);
        Graphics2D g = (Graphics2D) buff.getGraphics();
        g2r.layout(g, new Dimension(width, 1000));
        g.dispose();

        // get size
        Rectangle rect = g2r.getMinimumSize();

        // render into real buffer
        buff = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), bufferedImageType);
        g = (Graphics2D) buff.getGraphics();
        g2r.render(g);
        g.dispose();

        // return real buffer
        return buff;
    }

    public static BufferedImage createBuf(String filePath, int w) {

//Generate an image from a file:
        File f = new File(filePath);
        int width = 1024;
        int height = 1024;

// can specify width alone, or width + height
// constructing does not render; not until getImage() is called
        Java2DRenderer renderer = null;
        try {
            renderer = new Java2DRenderer(f, w);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

// this renders and returns the image, which is stored in the J2R; will not
// be re-rendered, calls to getImage() return the same instance
        return renderer.getImage();
    }

//    private Document loadDocument(final String uri) {
//        return sharedContext.getUac().getXMLResource(uri).getDocument();
//    }
    public static Image create(String filePath, int w) {

//Generate an image from a file:
        File f = new File(filePath);
        int width = 1024;
        int height = 1024;

// can specify width alone, or width + height
// constructing does not render; not until getImage() is called
        Java2DRenderer renderer = null;
        try {
            renderer = new Java2DRenderer(f, w);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

// this renders and returns the image, which is stored in the J2R; will not
// be re-rendered, calls to getImage() return the same instance
        BufferedImage img = renderer.getImage();

// write it out, full size, PNG
// FSImageWriter instance can be reused for different images,
// defaults to PNG
        FSImageWriter imageWriter = new FSImageWriter();
        try {
            imageWriter.write(img, "x-full.png");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

// write out as uncompressed JPEG (the 1f parameter)
// use convenience factory method; you can actually just pass in
// the type of image as a string but then you have to know how
// to make the compression calls without causing exceptions
// to be thrown :)
        imageWriter = FSImageWriter.newJpegWriter(1f);
        try {
            imageWriter.write(img, "nc.jpg");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


// now scale it
// ScalingOptions lets us control some quality options and pass in
// rendering hints
        ScalingOptions scalingOptions = new ScalingOptions(
                //BufferedImage.TYPE_INT_ARGB,
                DownscaleQuality.LOW_QUALITY,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

// target size--you can reuse the options instance for different sizes
        scalingOptions.setTargetDimensions(new Dimension(250, 250));
        Image scaled = ImageUtil.getScaledInstance(scalingOptions, img);

        return scaled;

// we can also scale multiple dimensions at once (well, not at once, but...)
// be careful because quality settings in the options instance can affect
// performance drastically
//        List dimensions = new ArrayList();
//        dimensions.add(new Dimension(100, 100));
//        dimensions.add(new Dimension(250, 250));
//        dimensions.add(new Dimension(500, 500));
//        dimensions.add(new Dimension(750, 750));
//        List images = ImageUtil.scaleMultiple(scalingOptions, img, dimensions);

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

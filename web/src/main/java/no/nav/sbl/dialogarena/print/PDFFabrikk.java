package no.nav.sbl.dialogarena.print;

import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;


public class PDFFabrikk {

    /**
     * Lag en pdf fra en html-string. Baseurl er adressen til mappen hvor css ligger.
     * PDFen skrives til en fil (adressen er i outputPdfPath).
     */
    public static void lagPdfFil(String html, String baseurl, String outputPdfPath)
            throws IOException, DocumentException {
        OutputStream os = null;

        if (!baseurl.contains("://")) {
            baseurl = getBaseUrlString(baseurl);
        }
        try {
            os = new FileOutputStream(outputPdfPath);

            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(html, baseurl);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Lag en pdf fra en html-string. Baseurl er adressen til mappen hvor css ligger.
     * PDFen skrives til OutputStream.
     */
    public static OutputStream lagPDFOutputStream(String html, String baseurl, OutputStream output)
            throws IOException, DocumentException {

        baseurl = getBaseUrlString(baseurl);
        ITextRenderer renderer = new ITextRenderer();

        renderer.setDocumentFromString(html, baseurl);
        renderer.layout();
        renderer.createPDF(output);
        return output;
    }

    /**
     * Gjør om en url i en string til en URL, hvis det er en fil.
     *
     */
    private static String getBaseUrlString(String baseurl) throws MalformedURLException {
        File f = new File(baseurl);
        if (f.exists()) {
            baseurl = f.toURI().toURL().toString();
        }
        return baseurl;
    }
}

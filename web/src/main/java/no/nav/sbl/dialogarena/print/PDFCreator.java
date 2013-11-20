package no.nav.sbl.dialogarena.print;

import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;


public class PDFCreator {


    public static void createPDF(String html, String pdf, String baseurl)
            throws IOException, DocumentException {
        OutputStream os = null;

        if (!baseurl.contains("://")) {
            baseurl = getBaseUrlString(baseurl);
        }
        try {
            os = new FileOutputStream(pdf);

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

    public static OutputStream createPDF(String html, String baseurl, OutputStream os)
            throws IOException, DocumentException {

        baseurl = getBaseUrlString(baseurl);
        try {
            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(html, baseurl);
            renderer.layout();
            renderer.createPDF(os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return os;
    }

    private static String getBaseUrlString(String baseurl) throws MalformedURLException {
        File f = new File(baseurl);
        if (f.exists()) {
            baseurl = f.toURI().toURL().toString();
        }
        return baseurl;
    }
}

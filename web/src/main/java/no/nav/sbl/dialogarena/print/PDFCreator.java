package no.nav.sbl.dialogarena.print;

import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;


public class PDFCreator {


    public static void createPDF(String html, String pdf, String baseurl)
            throws IOException, DocumentException {
        OutputStream os = null;

        if (baseurl.indexOf("://") == -1) {
            File f = new File(baseurl);
            if (f.exists()) {
                baseurl = f.toURI().toURL().toString();
            }
        }

        try {
            long start = new Date().getTime();
            os = new FileOutputStream(pdf);

            ITextRenderer renderer = new ITextRenderer();

//            Document dom = XMLResource.load(new InputSource(new BufferedReader(new StringReader(html)))).getDocument();
//            renderer.setDocument(dom, baseurl);

            renderer.setDocumentFromString(html, baseurl);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;
            long stop  = new Date().getTime();
            long diff = stop - start;
            System.out.println("diff = " + diff);
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

        File f = new File(baseurl);
        if (f.exists()) {
            baseurl = f.toURI().toURL().toString();
        }
        try {
            long start = new Date().getTime();

            ITextRenderer renderer = new ITextRenderer();

            //Document doc = XMLResource.load(new InputSource(new StringReader(html))).getDocument();

            renderer.setDocumentFromString(html, baseurl);
            renderer.layout();
            renderer.createPDF(os);

            long stop  = new Date().getTime();
            long diff = stop - start;
            System.out.println("diff = " + diff);
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
}

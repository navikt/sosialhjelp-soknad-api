package no.nav.sbl.sosialhjelp.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;
import org.apache.commons.io.IOUtils;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextRenderer;


import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

public class PDFFabrikk {

    /**
     * Lag en pdf fra en html-string.
     */
    public static byte[] lagPdfFil(String html, String skjemaPath) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer() {

            };
            renderer.setListener(new DefaultPDFCreationListener() {
                @Override
                public void preOpen(ITextRenderer iTextRenderer) {
                    iTextRenderer.getWriter().setPDFXConformance(PdfWriter.PDFA1A);
                    iTextRenderer.getWriter().createXmpMetadata();

                    super.preOpen(iTextRenderer);
                }
            });

            renderer.setDocumentFromString(html, skjemaPath);
            
            renderer.getFontResolver().addFont("/fonts/modus/ModusRegular.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/modus/ModusLight.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/modus/ModusBold.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/modus/ModusSemiBold.ttf", "Modus", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/arial/arial.ttf", "ArialSystem", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/arial/arialbd.ttf", "ArialSystem", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/Arial-BoldMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/Arial-BoldMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/Arial-BoldItalicMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.getFontResolver().addFont("/fonts/Arial-BoldItalicMT.ttf", "Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null);
            renderer.layout();
            renderer.setPDFVersion(PdfWriter.VERSION_1_4);
            renderer.createPDF(os, false, 0);
            InputStream resourceAsStream = PDFFabrikk.class.getClassLoader().getResourceAsStream("AdobeRGB1998.icc");
            byte[] byteArray = IOUtils.toByteArray(resourceAsStream);
            renderer.getWriter().setOutputIntents(
                    "Custom",
                    "PDF/A", "http://www.color.org",
                    "AdobeRGB1998",
                    byteArray);
            renderer.finishPDF();
        } catch (DocumentException|IOException e) {
            throw new SosialhjelpSoknadApiException("Kunne ikke lagre oppsummering som PDF", e);
        }
        return os.toByteArray();
    }
    
    private static String inPdfBox(String path) {
        return PDFFabrikk.class.getResource(path).toString();
    }
}

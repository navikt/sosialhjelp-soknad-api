package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PdfValidator {

    private static final Logger logger = LoggerFactory.getLogger(PdfValidator.class);

    PDDocument document;

    public PdfValidator(PDDocument document)  {
        this.document = document;
    }

    public boolean isSigned() {
        try {
            return !document.getSignatureDictionaries().isEmpty();
        } catch (IOException e) {
            logger.error("Kunne ikke lese siganturinformasjon fra PDF");
            throw new RuntimeException(e);
        }
    }

    public boolean isEncrypted() {
        return document.isEncrypted();
    }
}
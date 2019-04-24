package no.nav.sbl.sosialhjelp.pdf;


import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfValidatorTest {

    private static final String testPdfDirectory = "src/test/resources/pdf/";
    private Path gyldigPdfA1b = Paths.get(testPdfDirectory, "gyldig-pdfa1b.pdf");
    private Path pdfSomIkkeValiderer = Paths.get(testPdfDirectory, "ikke-gyldig-pdfa-har-lenke.pdf");

    @Test
    public void gyldigePdfSkalIkkeGiException() throws IOException {
        PdfValidator.assertValidPdfA(Files.readAllBytes(gyldigPdfA1b));
    }
    
    @Test(expected=IllegalStateException.class)
    public void skalGiExceptionVedUgyldigPdfA1b() throws IOException {
        PdfValidator.assertValidPdfA(Files.readAllBytes(pdfSomIkkeValiderer));
    }
}

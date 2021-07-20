package no.nav.sosialhjelp.soknad.business.pdf;

import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Disabled
class ApachePDFBoxPreflightTest {

    private static final String pathToDir = "src/test/java/no/nav/sbl/dialogarena/utils";

    private Path pdfUtenVannmerkePath;
    private File pdfUtenVannmerkeFile;
    private Path pdfMedVannmerkePath;
    private File pdfMedVannmerkeFile;

    @BeforeEach
    public void setup() {
        pdfUtenVannmerkePath = Paths.get(pathToDir + "/pdfUtenVannmerke.pdf");
        pdfUtenVannmerkeFile = new File(pdfUtenVannmerkePath.toString());
        pdfMedVannmerkePath = Paths.get(pathToDir + "/pdfMedVannmerke.pdf");
        pdfMedVannmerkeFile = new File(pdfMedVannmerkePath.toString());
    }

    @Test
    void testApachePDFBoxPreflightUtenVannmarke() throws IOException {
        ValidationResult result = getValidationResult(pdfUtenVannmerkeFile.toString());

        printValidationResult(result, pdfUtenVannmerkeFile);
    }

    @Test
    void testApachePDFBoxPreflightMedVannmerke() throws IOException {
        ValidationResult result = getValidationResult(pdfMedVannmerkeFile.toString());

        printValidationResult(result, pdfMedVannmerkeFile);
    }

    private void printValidationResult(ValidationResult result, File fil) {
        if (result.isValid()) {
            System.out.println("The file " + fil + " is a valid PDF/A-1b file");
        } else {
            System.out.println("The file" + fil + " is not valid, error(s) :");
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
        }
    }

    private ValidationResult getValidationResult(String filename) throws IOException {
        ValidationResult result = null;

        PreflightParser parser = new PreflightParser(filename);
        try {

            /* Parse the PDF file with PreflightParser that inherits from the NonSequentialParser.
             * Some additional controls are present to check a set of PDF/A requirements.
             * (Stream length consistency, EOL after some Keyword...)
             */
            parser.parse();

            /* Once the syntax validation is done,
             * the parser can provide a PreflightDocument
             * (that inherits from PDDocument)
             * This document process the end of PDF/A validation.
             */
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();

            // Get validation result
            result = document.getResult();
            document.close();

        } catch (SyntaxValidationException e) {
            /* the parse method can throw a SyntaxValidationException
             * if the PDF file can't be parsed.
             * In this case, the exception contains an instance of ValidationResult
             */
            result = e.getResult();
        } catch (ValidationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
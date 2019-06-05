package no.nav.sbl.sosialhjelp.pdf;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

public final class PdfValidator {
    private static final Logger logger = LoggerFactory.getLogger(PdfValidator.class);
    
    static {
        VeraGreenfieldFoundryProvider.initialise();
    }

    private PdfValidator() {}
    
    
    public static void softAssertValidPdfA(byte[] pdf) {
        try {
            assertValidPdfA(pdf);
        } catch (RuntimeException e) {
            if (isProduction()) {
                logger.error("Feil ved validering av PDF.", e);
            } else {
                throw e;
            }
        }
    }
    
    public static void assertValidPdfA(byte[] pdf) {
        final ValidationResult result = validatePdfA1b(pdf);
        if (result.isCompliant()) {
            return;
        }
        
        final String failedAssertions = toFailAssertionsString(result);
        throw new IllegalStateException("Ugyldig PDF/A-1b. Det er trolig brukt innhold som ikke er tillatt i PDF/A-1b som eksterne hyperlenker. Se valideringsfeil:\n" + failedAssertions);
    }

    
    private static String toFailAssertionsString(final ValidationResult result) {
        final StringBuilder sb = new StringBuilder();
        for (TestAssertion testAssertion : result.getTestAssertions()) {
            if (testAssertion.getStatus() == TestAssertion.Status.FAILED) {
                sb.append(testAssertion.toString() + "\n\n");
            }
        }
        return sb.toString();
    }

    private static ValidationResult validatePdfA1b(byte[] pdf) {
        try {
            final PDFAFlavour flavour = PDFAFlavour.fromString("1b");
            final PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
            final PDFAParser parser = Foundries.defaultInstance().createParser(new ByteArrayInputStream(pdf), flavour);
            return validator.validate(parser);
        } catch (ModelParsingException e) {
            throw new IllegalStateException("Klarer ikke å parse PDF ved validering. Dette skal ikke kunne skje -- det er trolig en feil med PDF-generering.", e);
        } catch (EncryptedPdfException e) {
            throw new IllegalStateException("PDF kan ikke valideres når den er kryptert. Dette skal ikke kunne skje -- det er trolig en feil med PDF-generering.", e);
        } catch (ValidationException e) {
            throw new RuntimeException("Ukjent feil ved forsøk på validering av PDF.", e);
        }
    }
    
    private static boolean isProduction() {
        return "p".equals(System.getProperty("environment.name"));
    }
}

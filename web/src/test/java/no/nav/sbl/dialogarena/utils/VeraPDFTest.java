package no.nav.sbl.dialogarena.utils;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.verapdf.pdfa.validation.profiles.ProfileDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class VeraPDFTest {

    private static final String pathToDir = "src/test/java/no/nav/sbl/dialogarena/utils";
    private static final boolean logResult = true;

    private PDFAParser parser;
    private PDFAFlavour flavour;
    private Path pdfUtenVannmerkePath;
    private File pdfUtenVannmerkeFile;
    private Path pdfMedVannmerkePath;
    private File pdfMedVannmerkeFile;
    private ValidationResult result;
    private ProfileDetails profileDetails;
    private Set<TestAssertion> testAssertions;


    @Before
    public void setup() {

        VeraGreenfieldFoundryProvider.initialise();
        pdfUtenVannmerkePath = Paths.get(pathToDir + "/pdfUtenVannmerke.pdf");
        pdfUtenVannmerkeFile = new File(pdfUtenVannmerkePath.toString());
        pdfMedVannmerkePath = Paths.get(pathToDir + "/pdfMedVannmerke.pdf");
        pdfMedVannmerkeFile = new File(pdfMedVannmerkePath.toString());
    }

    @Test
    public void testVeraPDFUtenVannmerkePDFA1B() {

        flavour = PDFAFlavour.fromString("1b");
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, logResult);

        result = runPDFTest(pdfUtenVannmerkeFile, validator);

        Assert.assertTrue("File should be a valid PDF/A " + result.getPDFAFlavour().toString() + "!", result.isCompliant());
    }

    @Test
    public void testVeraPDFMedVannmerkePDFA1B() {

        flavour = PDFAFlavour.fromString("1b");
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, logResult);

        result = runPDFTest(pdfMedVannmerkeFile, validator);

        Assert.assertTrue("File should be a valid PDF/A " + result.getPDFAFlavour().toString() + "!", result.isCompliant());
    }

    @Test
    public void testVeraPDFMedVannmerkePDFA1A() {

        flavour = PDFAFlavour.fromString("1a");
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, logResult);

        result = runPDFTest(pdfMedVannmerkeFile, validator);

        Assert.assertFalse("File is not a valid PDF/A " + result.getPDFAFlavour().toString() + "!", result.isCompliant());
    }
    @Test
    public void testVeraPDUtenVannmerkePDFA1A() {

        flavour = PDFAFlavour.fromString("1a");
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, logResult);

        result = runPDFTest(pdfUtenVannmerkeFile, validator);

        Assert.assertFalse("File is not a valid PDF/A " + result.getPDFAFlavour().toString() + "!", result.isCompliant());
    }

    private ValidationResult runPDFTest(File pdf, PDFAValidator validator) {
        try {
            parser = Foundries.defaultInstance().createParser(new FileInputStream(pdf), flavour);

            result = validator.validate(parser);

            profileDetails = result.getProfileDetails();

            testAssertions = result.getTestAssertions();

            for (TestAssertion testAssertion : testAssertions) {

                System.out.println("Test Assertion : " + testAssertion.getMessage());
            }

            System.out.println("\n\nProfile details : " + profileDetails.getName());

            if (result.isCompliant()) {
                // File is valid
                System.out.println("File is a valid PDF/A " + result.getPDFAFlavour().toString());
            } else {
                System.out.println("File is NOT(!) a valid PDF/A " + result.getPDFAFlavour().toString());
            }

        } catch (ModelParsingException | EncryptedPdfException | FileNotFoundException | ValidationException e) {
            e.printStackTrace();
        }
        return result;
    }
}

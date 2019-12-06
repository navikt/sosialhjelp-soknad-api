package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.*;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import sun.security.ssl.Debug;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.*;

@Component
public class SosialhjelpPdfGenerator {

    private PdfGenerator pdfGen;

    private static final float FONT_SIZE = 12;
    private static final float FONT_SIZE_BOLD = 16;
    public static final float PAGE_WIDTH = 400;
    public static final float PAGE_HEIGHT = 500;

//    private static final byte[] NAV_LOGO = logo();

    @Inject
    private NavMessageSource navMessageSource;

    @Inject
    public SosialhjelpPdfGenerator() {
        pdfGen = new PdfGenerator();
    }

    public byte[] generate(JsonInternalSoknad jsonInternalSoknad) {
        try {
            PDDocument doc = new PDDocument();
            PDPage page1 = pdfGen.newPage();
            PDPage page2 = pdfGen.newPage();
            PDPageContentStream cos1 = new PDPageContentStream(doc, page1);
            PDPageContentStream cos2 = new PDPageContentStream(doc, page2);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();


            float y = PdfGenerator.calculateStartY();

            Debug debug = new Debug();
            debug.println("calculated start y = " + calculateStartY());

            debug.println("y før header = " + y);

            // write stuff
            String heading = "Søknad om økonomisk sosialhjelp";

            // Navn og fnr i header
            JsonData data = jsonInternalSoknad.getSoknad().getData();
            JsonPersonalia jsonPersonalia = data.getPersonalia(); // personalia er required

            JsonPersonIdentifikator jsonPersonIdentifikator = jsonPersonalia.getPersonIdentifikator(); // required
            JsonSokernavn jsonSokernavn = jsonPersonalia.getNavn();// required


            String navn = jsonSokernavn.getFornavn() + " " + jsonSokernavn.getMellomnavn() + " " + jsonSokernavn.getEtternavn();
            String fnr = jsonPersonIdentifikator.getVerdi(); // required
            y -= header(doc, cos1, y, heading, navn, fnr);

            // FIX logoen
//            y -= addLogo(doc, cos1, y);

            y = leggTilPersonalia(doc, cos1, y, jsonPersonalia);
            y = leggTilBegrunnelse(doc, cos1, y, data.getBegrunnelse());

            // FIXME:
            float y2 = calculateStartY();
            y2 = leggTilArbeidOgUtdanning(doc, cos2, y2, data.getArbeid(), data.getUtdanning());





            doc.addPage(page1);
            doc.addPage(page2);

            cos1.close();
            cos2.close();
            doc.save(baos);
            byte[] pdf;
            pdf = baos.toByteArray();
            return pdf;

        } catch (IOException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }



    public static float addBlankLine(PDPageContentStream cos, float y) {
        return 20;
    }

    private float leggTilPersonalia(PDDocument doc, PDPageContentStream cos, float y, JsonPersonalia jsonPersonalia) throws IOException {

        y -= addBlankLine(cos, y);
        y -= skrivH4Bold(cos, y, "Personopplysninger");
        y -= addBlankLine(cos, y);

        // Statsborgerskap
        JsonStatsborgerskap jsonStatsborgerskap = jsonPersonalia.getStatsborgerskap();
        y -= skrivTekstBold(cos, y, "Statsborgerskap");
        if (jsonStatsborgerskap != null && jsonStatsborgerskap.getVerdi() != null) {
            String statsborgerskap = jsonPersonalia.getStatsborgerskap().getVerdi();
            y -= skrivTekst(cos, y, statsborgerskap);
        }
        y -= addBlankLine(cos, y);

        // Adresse
        y -= skrivTekstBold(cos, y, "Adresse");
        y -= addBlankLine(cos, y);

        if (jsonPersonalia.getFolkeregistrertAdresse() != null) {
            y -= skrivTekst(cos, y, "Folkeregistrert adresse:");
            String folkeregistrertAdresseTekst = "";
            switch (jsonPersonalia.getFolkeregistrertAdresse().getType()) {
                case GATEADRESSE:
                    JsonGateAdresse gaf = (JsonGateAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = gaf.getGatenavn() + " " + gaf.getHusnummer() + gaf.getHusbokstav() + ", " + gaf.getPostnummer() + " " + gaf.getPoststed();
                    break;
                case MATRIKKELADRESSE:
                    JsonMatrikkelAdresse maf = (JsonMatrikkelAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = "Bruksnummer: " + maf.getBruksnummer() + ". Gårdsnummer: " + maf.getGaardsnummer() + ". Kommunenummer:" + maf.getKommunenummer() + ".";
                    break;
                case POSTBOKS:
                    JsonPostboksAdresse pbaf = (JsonPostboksAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = "Postboks: " + pbaf.getPostboks() + ", " + pbaf.getPostnummer() + " " + pbaf.getPoststed();
                    break;
                case USTRUKTURERT:
                    JsonUstrukturertAdresse uaf = (JsonUstrukturertAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = " " + String.join(" ", uaf.getAdresse());
                    break;
            }
            y -= skrivTekstMedInnrykk(cos, y, folkeregistrertAdresseTekst, INNRYKK_2);
        }

        if (jsonPersonalia.getOppholdsadresse() != null) {
            y -= skrivTekst(cos, y, "Oppgi adressen der du bor");
            String oppholdsAdresseTekst = "";
            switch (jsonPersonalia.getOppholdsadresse().getType()) {
                case GATEADRESSE:
                    JsonGateAdresse gaf = (JsonGateAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = gaf.getGatenavn() + " " + gaf.getHusnummer() + gaf.getHusbokstav() + ", " + gaf.getPostnummer() + " " + gaf.getPoststed();
                    break;
                case MATRIKKELADRESSE:
                    JsonMatrikkelAdresse maf = (JsonMatrikkelAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = "Bruksnummer: " + maf.getBruksnummer() + ". Gårdsnummer: " + maf.getGaardsnummer() + ". Kommunenummer:" + maf.getKommunenummer() + ".";
                    break;
                case POSTBOKS:
                    JsonPostboksAdresse pbaf = (JsonPostboksAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = "Postboks: " + pbaf.getPostboks() + ", " + pbaf.getPostnummer() + " " + pbaf.getPoststed();
                    break;
                case USTRUKTURERT:
                    JsonUstrukturertAdresse uaf = (JsonUstrukturertAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = " " + String.join(" ", uaf.getAdresse());
                    break;
            }
            y -= skrivTekstMedInnrykk(cos, y, oppholdsAdresseTekst, INNRYKK_2);
        }


        y -= addBlankLine(cos, y);


        // Telefonnummer
        JsonTelefonnummer jsonTelefonnummer = jsonPersonalia.getTelefonnummer();
        if (jsonTelefonnummer != null) {
            y -= skrivTekstBold(cos, y, "Telefonnummer");
            y -= skrivTekst(cos, y, "Norsk telefonnummer (8 siffer)");
            y -= skrivTekstMedInnrykk(cos, y, jsonTelefonnummer.getVerdi(), INNRYKK_2);
        }

        y -= addBlankLine(cos, y);

        // Kontonummer
        JsonKontonummer jsonKontonummer = jsonPersonalia.getKontonummer();
        if (jsonKontonummer != null) {
            y -= addBlankLine(cos, y);
            y -= skrivTekstBold(cos, y, "Kontonummer");
            y -= skrivTekst(cos, y, "Kontonummer (11 siffer)");
            y -= skrivTekstMedInnrykk(cos, y, jsonKontonummer.getVerdi(), INNRYKK_2);
        }

        return y;
    }

    private float leggTilBegrunnelse(PDDocument doc, PDPageContentStream cos1, float y, JsonBegrunnelse jsonBegrunnelse) throws IOException {

        y -= addBlankLine(cos1, y);

        y -= skrivH4Bold(cos1, y, "Hva søker du om");

        y -= addBlankLine(cos1, y);

        y -= skrivTekstBold(cos1, y, "Beskriv kort hva du søker om");
        y -= skrivTekst(cos1, y, jsonBegrunnelse.getHvaSokesOm());

        y -= addBlankLine(cos1, y);

        y -= skrivTekstBold(cos1, y, "Gi en kort begrunnelse for søknaden");
        y -= skrivTekst(cos1, y, jsonBegrunnelse.getHvorforSoke());

        y -= addBlankLine(cos1, y);

        return y;
    }

    private float leggTilArbeidOgUtdanning(PDDocument doc, PDPageContentStream cos, float y, JsonArbeid arbeid, JsonUtdanning utdanning) throws IOException {

        y -= addBlankLine(cos, y);
        y -= skrivH4Bold(cos, y, "Arbeid og utdanning");
        y -= addBlankLine(cos, y);
        y -= skrivTekstBold(cos, y, "Dine arbeidsforhold");
        y -= addBlankLine(cos, y);

        if (arbeid != null && arbeid.getForhold() != null && arbeid.getForhold().size() > 0) {

            List<JsonArbeidsforhold> forholdsliste = arbeid.getForhold();
            for ( JsonArbeidsforhold forhold : forholdsliste) {
                if (forhold.getArbeidsgivernavn() != null) {
                    y -= skrivTekst(cos, y, "Arbeidsgiver: " + forhold.getArbeidsgivernavn());
                }
                if (forhold.getStillingstype() != null) {
                    y -= skrivTekst(cos, y, "Stillingstype: " + forhold.getStillingstype());
                }
                if (forhold.getStillingsprosent() != null) {
                    y -= skrivTekst(cos, y, "Stillingsprosent: " + forhold.getStillingsprosent());
                }
                if (forhold.getFom() != null) {
                    y -= skrivTekst(cos, y, "Startdato: " + forhold.getFom());
                }
                if (forhold.getTom() != null) {
                    y -= skrivTekst(cos, y, "Sluttdato: " + forhold.getTom());
                }
                y -= addBlankLine(cos, y);
            }

        } else {
            y -= skrivTekst(cos, y, "Vi har ingen registrerte arbeidsforhold på deg fra Arbeidsgiver- og arbeidstakerregisteret siste tre månedene.");
        }


        y -= skrivTekstBold(cos, y, "Utdanning");
        y -= addBlankLine(cos, y);
        y -= skrivTekstBold(cos, y, "Er du skoleelev eller student?");
        if (utdanning != null && utdanning.getErStudent() != null) {
            if (utdanning.getErStudent()) {
                y -= skrivTekst(cos, y, "Ja");
                if (utdanning.getStudentgrad() != null) {
                    y -= skrivTekst(cos, y, "Studentgrad: " + utdanning.getStudentgrad());
                }
            } else {
                y -= skrivTekst(cos, y, "Nei");
            }
        } else {
            y -= skrivTekstKursiv(cos, y, "Ikke utfylt");
        }
        y -= addBlankLine(cos, y);

        return y;
    }




    private float skrivTekst(PDPageContentStream cos, float y, String text) throws IOException {
        return addParagraph(cos, y, text, FONT_PLAIN, FONT_SIZE, MARGIN);
    }

    private float skrivTekstKursiv(PDPageContentStream cos, float y, String text) throws IOException {
        return addParagraph(cos, y, text, FONT_KURSIV, FONT_SIZE, MARGIN);
    }

    private float skrivTekstMedInnrykk(PDPageContentStream cos, float y, String text, int innrykk) throws IOException {
        return addParagraph(cos, y, text, FONT_PLAIN, FONT_SIZE, innrykk);
    }

    private float skrivTekstBold(PDPageContentStream cos, float y, String tekst) throws IOException {
        return addParagraph(cos, y, tekst, FONT_BOLD, FONT_SIZE, MARGIN);
    }

    private float skrivH4Bold(PDPageContentStream cos, float y, String tekst) throws IOException {
        return addParagraph(cos, y, tekst, FONT_BOLD, FONT_SIZE_BOLD, MARGIN);
    }

    private float header(PDDocument doc, PDPageContentStream cos, float y, String heading, String navn, String fnr) throws IOException {
        float startY = y;
        y -= pdfGen.addCenteredH1Bold(cos, y, heading);
        y -= pdfGen.addCenteredH4Bold(cos, y, navn);
        y -= pdfGen.addCenteredH4Bold(cos, y, fnr);
        y -= pdfGen.addDividerLine(cos, y);
        return startY - y;
    }

//    public float addLogo(PDDocument doc, PDPageContentStream cos, float startY) throws IOException {
//        PDImageXObject ximage = PDImageXObject.createFromByteArray(doc, NAV_LOGO, "logo");
//        float startX = (MEDIA_BOX.getWidth() - 99) / 2;
//        float offsetTop = 40;
//        startY -= 62f / 2 + offsetTop;
//        cos.drawImage(ximage, startX, startY, 99, 62);
//        return 62 + offsetTop;
//    }

    private static byte[] logo() {
        try {
            return StreamUtils.copyToByteArray(new ClassPathResource("/pdf/nav-logo_alphaless.png").getInputStream());
        } catch (IOException e) {
            // FIXME: Handle it
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] pracGenerate() {

        try (PDDocument doc = new PDDocument()) {

            PdfGenerator pdfGen = new PdfGenerator();

            PDPage page = pdfGen.newPage();
            PDPage page2 = pdfGen.newPage();
            doc.addPage(page);
            doc.addPage(page2);
            PDPageContentStream contentStreamPage1 = new PDPageContentStream(doc, page);
            PDPageContentStream contentStreamPage2 = new PDPageContentStream(doc, page2);


            PDRectangle mediaBox = page.getMediaBox();
            float marginY = 80;
            float marginX = 60;
            float width = mediaBox.getWidth() - 2 * marginX;
            float startX = mediaBox.getLowerLeftX() + marginX;
            float startY = mediaBox.getUpperRightY() - marginY;

            String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat" +
                    " non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";


            contentStreamPage1.lineTo(0, 0);
            contentStreamPage1.closeAndStroke();
            contentStreamPage1.addLine(5, 5, PAGE_WIDTH, PAGE_HEIGHT);

            contentStreamPage1.beginText();
            addParagraph(contentStreamPage1, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, MARGIN);
            addParagraph(contentStreamPage1, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, MARGIN);
            addParagraph(contentStreamPage1, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, MARGIN);
            contentStreamPage1.endText();
            contentStreamPage1.close();

            contentStreamPage2.beginText();
            addParagraph(contentStreamPage1, startY, text, PdfGenerator.FONT_PLAIN, PdfGenerator.FONT_PLAIN_SIZE, MARGIN);
            contentStreamPage2.endText();
            contentStreamPage2.close();


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            System.err.println("Exception while trying to create pdf document - " + e);
        }

        return null;

    }


}

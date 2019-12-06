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
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.*;

@Component
public class SosialhjelpPdfGenerator {

    @Inject
    private NavMessageSource navMessageSource;

    public byte[] generate(JsonInternalSoknad jsonInternalSoknad) {
        try {

            PdfGenerator pdf = new PdfGenerator();

            JsonData data = jsonInternalSoknad.getSoknad().getData();
            JsonPersonalia jsonPersonalia = data.getPersonalia(); // personalia er required

            // Add header
            String heading = "Søknad om økonomisk sosialhjelp";
            JsonPersonIdentifikator jsonPersonIdentifikator = jsonPersonalia.getPersonIdentifikator(); // required
            JsonSokernavn jsonSokernavn = jsonPersonalia.getNavn();// required
            String navn = jsonSokernavn.getFornavn() + " " + jsonSokernavn.getMellomnavn() + " " + jsonSokernavn.getEtternavn();
            String fnr = jsonPersonIdentifikator.getVerdi(); // required
            pdf.addHeading(heading, navn, fnr);

            // FIX logoen
            // addLogo(doc, cos1, y);

            leggTilPersonalia(pdf, jsonPersonalia);
            leggTilBegrunnelse(pdf, data.getBegrunnelse());
            leggTilArbeidOgUtdanning(pdf, data.getArbeid(), data.getUtdanning());

            return pdf.finish();

        } catch (IOException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }


    private void leggTilPersonalia(PdfGenerator pdf, JsonPersonalia jsonPersonalia) throws IOException {

        pdf.addBlankLine();
        pdf.skrivH4Bold("Personopplysninger");
        pdf.addBlankLine();

        // Statsborgerskap
        JsonStatsborgerskap jsonStatsborgerskap = jsonPersonalia.getStatsborgerskap();
        pdf.skrivTekstBold("Statsborgerskap");
        if (jsonStatsborgerskap != null && jsonStatsborgerskap.getVerdi() != null) {
            String statsborgerskap = jsonPersonalia.getStatsborgerskap().getVerdi();
            pdf.skrivTekst(statsborgerskap);
        }
        pdf.addBlankLine();

        // Adresse
        pdf.skrivTekstBold("Adresse");
        pdf.addBlankLine();

        if (jsonPersonalia.getFolkeregistrertAdresse() != null) {
            pdf.skrivTekst("Folkeregistrert adresse:");
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
            pdf.skrivTekstMedInnrykk(folkeregistrertAdresseTekst, INNRYKK_2);
        }

        if (jsonPersonalia.getOppholdsadresse() != null) {
            pdf.skrivTekst("Oppgi adressen der du bor");
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
            pdf.skrivTekstMedInnrykk(oppholdsAdresseTekst, INNRYKK_2);
        }

        pdf.addBlankLine();

        // Telefonnummer
        JsonTelefonnummer jsonTelefonnummer = jsonPersonalia.getTelefonnummer();
        if (jsonTelefonnummer != null) {
            pdf.skrivTekstBold("Telefonnummer");
            pdf.skrivTekst("Norsk telefonnummer (8 siffer)");
            pdf.skrivTekstMedInnrykk(jsonTelefonnummer.getVerdi(), INNRYKK_2);
        }

        pdf.addBlankLine();

        // Kontonummer
        JsonKontonummer jsonKontonummer = jsonPersonalia.getKontonummer();
        if (jsonKontonummer != null) {
            pdf.addBlankLine();
            pdf.skrivTekstBold("Kontonummer");
            pdf.skrivTekst("Kontonummer (11 siffer)");
            pdf.skrivTekstMedInnrykk(jsonKontonummer.getVerdi(), INNRYKK_2);
        }

    }

    private void leggTilBegrunnelse(PdfGenerator pdf, JsonBegrunnelse jsonBegrunnelse) throws IOException {
        pdf.addBlankLine();
        pdf.skrivH4Bold("Hva søker du om");
        pdf.addBlankLine();
        pdf.skrivTekstBold("Beskriv kort hva du søker om");
        pdf.skrivTekst(jsonBegrunnelse.getHvaSokesOm());
        pdf.addBlankLine();
        pdf.skrivTekstBold("Gi en kort begrunnelse for søknaden");
        pdf.skrivTekst(jsonBegrunnelse.getHvorforSoke());
        pdf.addBlankLine();
    }

    private void leggTilArbeidOgUtdanning(PdfGenerator pdf, JsonArbeid arbeid, JsonUtdanning utdanning) throws IOException {
        pdf.addBlankLine();
        pdf.skrivH4Bold("Arbeid og utdanning");
        pdf.addBlankLine();
        pdf.skrivTekstBold("Dine arbeidsforhold");
        pdf.addBlankLine();

        if (arbeid != null && arbeid.getForhold() != null && arbeid.getForhold().size() > 0) {

            List<JsonArbeidsforhold> forholdsliste = arbeid.getForhold();
            for ( JsonArbeidsforhold forhold : forholdsliste) {
                if (forhold.getArbeidsgivernavn() != null) {
                    pdf.skrivTekst("Arbeidsgiver: " + forhold.getArbeidsgivernavn());
                }
                if (forhold.getStillingstype() != null) {
                    pdf.skrivTekst("Stillingstype: " + forhold.getStillingstype());
                }
                if (forhold.getStillingsprosent() != null) {
                    pdf.skrivTekst("Stillingsprosent: " + forhold.getStillingsprosent());
                }
                if (forhold.getFom() != null) {
                    pdf.skrivTekst("Startdato: " + forhold.getFom());
                }
                if (forhold.getTom() != null) {
                    pdf.skrivTekst("Sluttdato: " + forhold.getTom());
                }
                pdf.addBlankLine();
            }

        } else {
            pdf.skrivTekst("Vi har ingen registrerte arbeidsforhold på deg fra Arbeidsgiver- og arbeidstakerregisteret siste tre månedene.");
        }

        pdf.skrivTekstBold("Utdanning");
        pdf.addBlankLine();
        pdf.skrivTekstBold("Er du skoleelev eller student?");
        if (utdanning != null && utdanning.getErStudent() != null) {
            if (utdanning.getErStudent()) {
                pdf.skrivTekst("Ja");
                if (utdanning.getStudentgrad() != null) {
                    pdf.skrivTekst("Studentgrad: " + utdanning.getStudentgrad());
                }
            } else {
                pdf.skrivTekst("Nei");
            }
        } else {
            pdf.skrivTekstKursiv("Ikke utfylt");
        }
        pdf.addBlankLine();
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
}

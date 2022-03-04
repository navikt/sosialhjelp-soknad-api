package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.soknad.pdf.PdfUtils;
import no.nav.sosialhjelp.soknad.pdf.TextHelpers;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.apache.commons.lang3.LocaleUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator.INNRYKK_2;
import static no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator.INNRYKK_4;
import static no.nav.sosialhjelp.soknad.pdf.ArbeidOgUtdanningKt.leggTilArbeidOgUtdanning;
import static no.nav.sosialhjelp.soknad.pdf.BegrunnelseKt.leggTilBegrunnelse;
import static no.nav.sosialhjelp.soknad.pdf.BosituasjonKt.leggTilBosituasjon;
import static no.nav.sosialhjelp.soknad.pdf.FamilieKt.leggTilFamilie;
import static no.nav.sosialhjelp.soknad.pdf.InformasjonFraForsideKt.leggTilInformasjonFraForsiden;
import static no.nav.sosialhjelp.soknad.pdf.InntektOgFormueKt.leggTilInntektOgFormue;
import static no.nav.sosialhjelp.soknad.pdf.JuridiskInformasjonKt.leggTilJuridiskInformasjon;
import static no.nav.sosialhjelp.soknad.pdf.MetainformasjonKt.leggTilMetainformasjon;
import static no.nav.sosialhjelp.soknad.pdf.OkonomiskeOpplysningerOgVedleggKt.leggTilOkonomiskeOpplysningerOgVedlegg;
import static no.nav.sosialhjelp.soknad.pdf.UtgifterOgGjeldKt.leggTilUtgifterOgGjeld;

public class SosialhjelpPdfGenerator {

    public final NavMessageSource navMessageSource;
    public final TextHelpers textHelpers;
    public final PdfUtils pdfUtils;

    public SosialhjelpPdfGenerator(NavMessageSource navMessageSource, TextHelpers textHelpers, PdfUtils pdfUtils) {
        this.navMessageSource = navMessageSource;
        this.textHelpers = textHelpers;
        this.pdfUtils = pdfUtils;
    }

    public byte[] generate(JsonInternalSoknad jsonInternalSoknad, boolean utvidetSoknad) {
        try {

            PdfGenerator pdf = new PdfGenerator();

            JsonData data = jsonInternalSoknad.getSoknad().getData();
            JsonPersonalia jsonPersonalia = data.getPersonalia(); // personalia er required

            // Add header
            String heading = getTekst("applikasjon.sidetittel");
            JsonPersonIdentifikator jsonPersonIdentifikator = jsonPersonalia.getPersonIdentifikator(); // required
            JsonSokernavn jsonSokernavn = jsonPersonalia.getNavn();// required

            String navn = getJsonNavnTekst(jsonSokernavn);

            String fnr = jsonPersonIdentifikator.getVerdi(); // required

            leggTilHeading(pdf, heading, navn, fnr);

            leggTilPersonalia(pdf, data.getPersonalia(), jsonInternalSoknad.getMidlertidigAdresse(), utvidetSoknad);
            leggTilBegrunnelse(pdf, pdfUtils, data.getBegrunnelse(), utvidetSoknad);
            leggTilArbeidOgUtdanning(pdf, pdfUtils, data.getArbeid(), data.getUtdanning(), utvidetSoknad);
            leggTilFamilie(pdf, pdfUtils, data.getFamilie(), utvidetSoknad);
            leggTilBosituasjon(pdf, pdfUtils, data.getBosituasjon(), utvidetSoknad);
            leggTilInntektOgFormue(pdf, pdfUtils, data.getOkonomi(), jsonInternalSoknad.getSoknad(), utvidetSoknad);
            leggTilUtgifterOgGjeld(pdf, pdfUtils, data.getOkonomi(), jsonInternalSoknad.getSoknad(), utvidetSoknad);
            leggTilOkonomiskeOpplysningerOgVedlegg(pdf, pdfUtils, data.getOkonomi(), jsonInternalSoknad.getVedlegg(), utvidetSoknad);
            leggTilInformasjonFraForsiden(pdf, pdfUtils, data.getPersonalia(), utvidetSoknad);
            leggTilJuridiskInformasjon(pdf, jsonInternalSoknad.getSoknad(), utvidetSoknad);
            leggTilMetainformasjon(pdf, jsonInternalSoknad.getSoknad());

            return pdf.finish();
        } catch (Exception e) {
            if (utvidetSoknad) {
                throw new PdfGenereringException("Kunne ikke generere Soknad-juridisk.pdf", e);
            }
            throw new PdfGenereringException("Kunne ikke generere Soknad.pdf", e);
        }
    }

    public byte[] generateEttersendelsePdf(JsonInternalSoknad jsonInternalSoknad, String eier) {
        try {
            PdfGenerator pdf = new PdfGenerator();

            String tittel = getTekst("ettersending.kvittering.tittel");
            String undertittel = getTekst("skjema.tittel");
            leggTilHeading(pdf, tittel, undertittel, eier);

            String pattern = "d. MMMM yyyy HH:mm";
            DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime currentTime = LocalDateTime.now();

            pdf.skrivTekstBold("Følgende vedlegg er sendt " + currentTime.format(format) + ":");
            pdf.addBlankLine();

            if (jsonInternalSoknad.getVedlegg() != null && jsonInternalSoknad.getVedlegg().getVedlegg() != null) {
                for (JsonVedlegg jsonVedlegg : jsonInternalSoknad.getVedlegg().getVedlegg()) {
                    if (jsonVedlegg.getStatus() != null && jsonVedlegg.getStatus().equals("LastetOpp")) {
                        pdf.skrivTekst(getTekst("vedlegg." + jsonVedlegg.getType() + "." + jsonVedlegg.getTilleggsinfo() + ".tittel"));
                        pdf.skrivTekst("Filer:");
                        for (JsonFiler jsonFiler : jsonVedlegg.getFiler()) {
                            pdf.skrivTekst("Filnavn: " + jsonFiler.getFilnavn());
                        }
                    }
                }
            }

            return pdf.finish();
        } catch (Exception e) {
            throw new PdfGenereringException("Kunne ikke generere ettersendelse.pdf", e);
        }
    }

    public byte[] generateBrukerkvitteringPdf() {
        try {
            PdfGenerator pdf = new PdfGenerator();

            leggTilHeading(pdf, "Brukerkvittering");

            pdf.skrivTekst("Fil ikke i bruk, generert for bakoverkompatibilitet med filformat / File not in use, generated for backward compatibility with fileformat");

            return pdf.finish();
        } catch (Exception e) {
            throw new PdfGenereringException("Kunne ikke generere Brukerkvittering.pdf", e);
        }
    }

    public String getTekst(String key) {
        return navMessageSource.getBundleFor("soknadsosialhjelp", LocaleUtils.toLocale("nb_NO")).getProperty(key);
    }

    private void leggTilHeading(PdfGenerator pdf, String heading, String... undertittler) throws IOException {
        pdf.addCenteredH1Bold(heading);
        for (String undertittel : undertittler) {
            if (undertittel != null && undertittel.length() > 0) {
                pdf.addCenteredH4Bold(undertittel);
            }
        }
        pdf.addDividerLine();
        pdf.addBlankLine();
    }

    private void leggTilPersonalia(PdfGenerator pdf, JsonPersonalia jsonPersonalia, JsonAdresse midlertidigAdresse, boolean utvidetSoknad) throws IOException {

        pdf.skrivH4Bold(getTekst("kontakt.tittel"));
        pdf.addBlankLine();

        if (utvidetSoknad) {
            pdf.skrivH4Bold(getTekst("kontakt.system.personalia.sporsmal"));
            pdfUtils.skrivInfotekst(pdf, "kontakt.system.personalia.infotekst.tekst");
        }

        // Statsborgerskap
        JsonStatsborgerskap jsonStatsborgerskap = jsonPersonalia.getStatsborgerskap();
        pdf.skrivTekstBold(getTekst("kontakt.system.personalia.statsborgerskap"));
        if (jsonStatsborgerskap != null) {
            String statsborgerskap = jsonPersonalia.getStatsborgerskap().getVerdi();
            pdf.skrivTekst(textHelpers.fulltNavnForLand(statsborgerskap));
        } else {
            pdf.skrivTekst(textHelpers.fulltNavnForLand(null));
        }
        pdf.addBlankLine();

        // Adresse
        if (utvidetSoknad) {
            pdf.skrivH4Bold(getTekst("soknadsmottaker.sporsmal"));
            pdfUtils.skrivHjelpetest(pdf, "soknadsmottaker.hjelpetekst.tekst");
        } else {
            pdf.skrivTekstBold(getTekst("kontakt.system.adresse"));
        }

        if (jsonPersonalia.getFolkeregistrertAdresse() != null) {
            pdf.skrivTekst(getTekst("kontakt.system.oppholdsadresse.folkeregistrertAdresse"));
            String folkeregistrertAdresseTekst = "";
            switch (jsonPersonalia.getFolkeregistrertAdresse().getType()) {
                case GATEADRESSE:
                    folkeregistrertAdresseTekst = jsonGateAdresseToString((JsonGateAdresse) jsonPersonalia.getFolkeregistrertAdresse());
                    break;
                case MATRIKKELADRESSE:
                    JsonMatrikkelAdresse maf = (JsonMatrikkelAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = getTekst("kontakt.system.adresse.bruksnummer.label") + ": " + maf.getBruksnummer() + ". " +
                            getTekst("kontakt.system.adresse.gaardsnummer.label") + ": " + maf.getGaardsnummer() + ". " +
                            getTekst("kontakt.system.adresse.kommunenummer.label") + ":" + maf.getKommunenummer() + ".";
                    break;
                case POSTBOKS:
                    JsonPostboksAdresse pbaf = (JsonPostboksAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = getTekst("kontakt.system.adresse.postboks.label") + ": " + pbaf.getPostboks() + ", " + pbaf.getPostnummer() + " " + pbaf.getPoststed();
                    break;
                case USTRUKTURERT:
                    JsonUstrukturertAdresse uaf = (JsonUstrukturertAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = " " + String.join(" ", uaf.getAdresse());
                    break;
            }
            pdf.skrivTekst(folkeregistrertAdresseTekst);
            pdf.addBlankLine();
        }

        if (jsonPersonalia.getOppholdsadresse() != null) {
            pdf.skrivTekst(getTekst("soknadsmottaker.infotekst.tekst"));
            String oppholdsAdresseTekst = "";
            switch (jsonPersonalia.getOppholdsadresse().getType()) {
                case GATEADRESSE:
                    oppholdsAdresseTekst = jsonGateAdresseToString((JsonGateAdresse) jsonPersonalia.getOppholdsadresse());
                    break;
                case MATRIKKELADRESSE:
                    JsonMatrikkelAdresse maf = (JsonMatrikkelAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = getTekst("kontakt.system.adresse.bruksnummer.label") + ": " + maf.getBruksnummer() + ". " +
                            getTekst("kontakt.system.adresse.gaardsnummer.label") + ": " + maf.getGaardsnummer() + ". " +
                            getTekst("kontakt.system.adresse.kommunenummer.label") + ":" + maf.getKommunenummer() + ".";
                    break;
                case POSTBOKS:
                    JsonPostboksAdresse pbaf = (JsonPostboksAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = getTekst("kontakt.system.adresse.postboks.label") + ": " + pbaf.getPostboks() + ", " + pbaf.getPostnummer() + " " + pbaf.getPoststed();
                    break;
                case USTRUKTURERT:
                    JsonUstrukturertAdresse uaf = (JsonUstrukturertAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = " " + String.join(" ", uaf.getAdresse());
                    break;
            }
            pdf.skrivTekst(oppholdsAdresseTekst);
            pdf.addBlankLine();
        }

        if (utvidetSoknad) {
            pdf.skrivTekst("Valgt adresse:");
            if (jsonPersonalia.getOppholdsadresse() != null) {
                JsonAdresseValg adresseValg = jsonPersonalia.getOppholdsadresse().getAdresseValg();
                if (adresseValg == JsonAdresseValg.SOKNAD) {
                    pdf.skrivTekstMedInnrykk(getTekst("kontakt.system.oppholdsadresse.valg.soknad"), INNRYKK_2);
                } else {
                    pdf.skrivTekstMedInnrykk(getTekst("kontakt.system.oppholdsadresse." + adresseValg.value() + "Adresse"), INNRYKK_2);
                }
                pdf.addBlankLine();

                pdf.skrivTekstBold("Svaralternativer:");
                if (jsonPersonalia.getFolkeregistrertAdresse() != null) {
                    pdf.skrivTekstMedInnrykk(getTekst("kontakt.system.oppholdsadresse.folkeregistrertAdresse"), INNRYKK_2);
                }
                if (midlertidigAdresse != null) {
                    pdf.skrivTekstMedInnrykk(getTekst("kontakt.system.oppholdsadresse.midlertidigAdresse"), INNRYKK_2);
                    if (adresseValg == JsonAdresseValg.MIDLERTIDIG) {
                        leggTilUtvidetInfoAdresse(pdf, jsonPersonalia.getOppholdsadresse());
                    } else {
                        leggTilUtvidetInfoAdresse(pdf, midlertidigAdresse);
                    }
                }
                pdf.skrivTekstMedInnrykk(getTekst("kontakt.system.oppholdsadresse.valg.soknad"), INNRYKK_2);
                pdf.addBlankLine();
            }
        }

        // Telefonnummer
        JsonTelefonnummer jsonTelefonnummer = jsonPersonalia.getTelefonnummer();
        if (jsonTelefonnummer != null) {
            if (jsonTelefonnummer.getKilde() == JsonKilde.SYSTEM) {
                pdf.skrivTekstBold(getTekst("kontakt.system.telefoninfo.sporsmal"));
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "kontakt.system.telefoninfo.infotekst.tekst");
                }
                pdf.skrivTekst(getTekst("kontakt.system.telefon.label"));
                pdf.skrivTekst(jsonTelefonnummer.getVerdi());
                pdf.addBlankLine();
                if (utvidetSoknad) {
                    pdfUtils.skrivKnappTilgjengelig(pdf, "kontakt.system.telefon.endreknapp.label");
                }
            } else {
                pdf.skrivTekstBold(getTekst("kontakt.telefon.sporsmal"));
                pdf.skrivTekst(getTekst("kontakt.telefon.label"));
                if (jsonTelefonnummer.getVerdi() == null || jsonTelefonnummer.getVerdi().isEmpty()) {
                    pdfUtils.skrivIkkeUtfylt(pdf);
                } else {
                    pdf.skrivTekst(jsonTelefonnummer.getVerdi());
                }
                pdf.addBlankLine();
                if (utvidetSoknad) {
                    pdfUtils.skrivKnappTilgjengelig(pdf, "systeminfo.avbrytendringknapp.label");
                    pdfUtils.skrivInfotekst(pdf, "kontakt.telefon.infotekst.tekst");
                }
            }
        }

        // Kontonummer
        JsonKontonummer jsonKontonummer = jsonPersonalia.getKontonummer();
        if (jsonKontonummer != null) {
            pdf.skrivTekstBold(getTekst("kontakt.kontonummer.sporsmal"));
            if (utvidetSoknad && jsonKontonummer.getKilde() == JsonKilde.SYSTEM) {
                pdfUtils.skrivInfotekst(pdf, "kontakt.system.personalia.infotekst.tekst");
            }
            if (jsonKontonummer.getKilde() == JsonKilde.SYSTEM) {
                pdf.skrivTekst(getTekst("kontakt.system.kontonummer.label"));
            } else {
                pdf.skrivTekst(getTekst("kontakt.kontonummer.label"));
            }
            if (jsonKontonummer.getHarIkkeKonto() != null && jsonKontonummer.getHarIkkeKonto()) {
                pdf.skrivTekst(getTekst("kontakt.kontonummer.harikke.true"));
            } else {
                if (jsonKontonummer.getVerdi() == null || jsonKontonummer.getVerdi().isEmpty()) {
                    pdfUtils.skrivIkkeUtfylt(pdf);
                } else {
                    pdf.skrivTekst(jsonKontonummer.getVerdi());
                }
            }
            pdf.addBlankLine();
            if (utvidetSoknad) {
                if (jsonKontonummer.getKilde() == JsonKilde.SYSTEM) {
                    pdfUtils.skrivKnappTilgjengelig(pdf, "kontakt.system.kontonummer.endreknapp.label");
                } else {
                    List<String> svaralternativer = new ArrayList<>(1);
                    svaralternativer.add("kontakt.kontonummer.harikke");
                    pdfUtils.skrivSvaralternativer(pdf, svaralternativer);
                    pdfUtils.skrivKnappTilgjengelig(pdf, "systeminfo.avbrytendringknapp.label");
                    pdfUtils.skrivInfotekst(pdf, "kontakt.kontonummer.infotekst.tekst");
                }
            }
        }
        pdf.addBlankLine();
    }

    private void leggTilUtvidetInfoAdresse(PdfGenerator pdf, JsonAdresse jsonAdresse) throws IOException {
        switch (jsonAdresse.getType()) {
            case GATEADRESSE:
                JsonGateAdresse jsonGateAdresse = (JsonGateAdresse) jsonAdresse;
                pdf.skrivTekstMedInnrykk(jsonGateAdresse.getGatenavn() + " " + jsonGateAdresse.getHusnummer() + "" + jsonGateAdresse.getHusbokstav(), INNRYKK_4);
                pdf.skrivTekstMedInnrykk(jsonGateAdresse.getPostnummer() + " " + jsonGateAdresse.getPoststed(), INNRYKK_4);
                break;
            case MATRIKKELADRESSE:
                JsonMatrikkelAdresse maf = (JsonMatrikkelAdresse) jsonAdresse;
                pdf.skrivTekstMedInnrykk(getTekst("kontakt.system.adresse.bruksnummer.label") + ": " + maf.getBruksnummer() + ". " +
                        getTekst("kontakt.system.adresse.gaardsnummer.label") + ": " + maf.getGaardsnummer() + ". " +
                        getTekst("kontakt.system.adresse.kommunenummer.label") + ":" + maf.getKommunenummer() + ".", INNRYKK_2);
                break;
            case POSTBOKS:
                JsonPostboksAdresse pbaf = (JsonPostboksAdresse) jsonAdresse;
                pdf.skrivTekstMedInnrykk(getTekst("kontakt.system.adresse.postboks.label") + ": " + pbaf.getPostboks() + ", " + pbaf.getPostnummer() + " " + pbaf.getPoststed(), INNRYKK_2);
                break;
            case USTRUKTURERT:
                JsonUstrukturertAdresse uaf = (JsonUstrukturertAdresse) jsonAdresse;
                pdf.skrivTekstMedInnrykk(String.join(" ", uaf.getAdresse()), INNRYKK_2);
        }
    }

    private String jsonGateAdresseToString(JsonGateAdresse gaf) {
        StringBuilder adresse = new StringBuilder();
        if (gaf.getGatenavn() != null) {
            adresse.append(gaf.getGatenavn()).append(" ");
        }
        if (gaf.getHusnummer() != null) {
            adresse.append(gaf.getHusnummer());
        }
        if (gaf.getHusbokstav() != null) {
            adresse.append(gaf.getHusbokstav());
        }
        adresse.append(", ");
        if (gaf.getPostnummer() != null) {
            adresse.append(gaf.getPostnummer()).append(" ");
        }
        if (gaf.getPoststed() != null) {
            adresse.append(gaf.getPoststed());
        }
        return adresse.toString();
    }

    private String getJsonNavnTekst(JsonNavn navn) {
        String fullstendigNavn = "";
        if (navn != null) {
            if (navn.getFornavn() != null) {
                fullstendigNavn += navn.getFornavn();
            }
            if (navn.getMellomnavn() != null) {
                fullstendigNavn += " " + navn.getMellomnavn();
            }
            if (navn.getEtternavn() != null) {
                fullstendigNavn += " " + navn.getEtternavn();
            }
        }
        return fullstendigNavn;
    }
}

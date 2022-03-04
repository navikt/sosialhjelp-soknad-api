package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.soknad.pdf.PdfUtils;
import no.nav.sosialhjelp.soknad.pdf.TextHelpers;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.apache.commons.lang3.LocaleUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator.INNRYKK_2;
import static no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator.INNRYKK_4;
import static no.nav.sosialhjelp.soknad.pdf.BosituasjonKt.leggTilBosituasjon;
import static no.nav.sosialhjelp.soknad.pdf.InformasjonFraForsideKt.leggTilInformasjonFraForsiden;
import static no.nav.sosialhjelp.soknad.pdf.InntektOgFormueKt.leggTilInntektOgFormue;
import static no.nav.sosialhjelp.soknad.pdf.JuridiskInformasjonKt.leggTilJuridiskInformasjon;
import static no.nav.sosialhjelp.soknad.pdf.MetainformasjonKt.leggTilMetainformasjon;
import static no.nav.sosialhjelp.soknad.pdf.OkonomiskeOpplysningerOgVedleggKt.leggTilOkonomiskeOpplysningerOgVedlegg;
import static no.nav.sosialhjelp.soknad.pdf.UtgifterOgGjeldKt.leggTilUtgifterOgGjeld;

public class SosialhjelpPdfGenerator {
    private final String DATO_FORMAT = "d. MMMM yyyy";

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
            leggTilBegrunnelse(pdf, data.getBegrunnelse(), utvidetSoknad);
            leggTilArbeidOgUtdanning(pdf, data.getArbeid(), data.getUtdanning(), utvidetSoknad);
            leggTilFamilie(pdf, data.getFamilie(), utvidetSoknad);
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

    private void leggTilBegrunnelse(PdfGenerator pdf, JsonBegrunnelse jsonBegrunnelse, boolean utvidetSoknad) throws IOException {
        pdf.skrivH4Bold(getTekst("begrunnelsebolk.tittel"));
        pdf.addBlankLine();

        pdf.skrivTekstBold(getTekst("begrunnelse.hva.sporsmal"));

        if (utvidetSoknad) {
            pdfUtils.skrivInfotekst(pdf, "begrunnelse.hva.infotekst.tekst");
        }
        if (jsonBegrunnelse.getHvaSokesOm() == null || jsonBegrunnelse.getHvaSokesOm().isEmpty()) {
            pdfUtils.skrivIkkeUtfylt(pdf);
        } else {
            pdf.skrivTekst(jsonBegrunnelse.getHvaSokesOm());
        }
        pdf.addBlankLine();

        pdf.skrivTekstBold(getTekst("begrunnelse.hvorfor.sporsmal"));
        if (jsonBegrunnelse.getHvorforSoke() == null || jsonBegrunnelse.getHvorforSoke().isEmpty()) {
            pdfUtils.skrivIkkeUtfylt(pdf);
        } else {
            pdf.skrivTekst(jsonBegrunnelse.getHvorforSoke());
        }
        pdf.addBlankLine();
    }

    private void leggTilArbeidOgUtdanning(PdfGenerator pdf, JsonArbeid arbeid, JsonUtdanning utdanning, boolean utvidetSoknad) throws IOException {
        pdf.skrivH4Bold(getTekst("arbeidbolk.tittel"));
        pdf.addBlankLine();

        pdf.skrivTekstBold(getTekst("arbeidsforhold.sporsmal"));
        pdf.addBlankLine();
        if (utvidetSoknad) {
            pdfUtils.skrivInfotekst(pdf, "arbeidsforhold.infotekst");
        }

        if (arbeid != null && arbeid.getForhold() != null) {
            for (JsonArbeidsforhold forhold : arbeid.getForhold()) {
                pdfUtils.skrivTekstMedGuard(pdf, forhold.getArbeidsgivernavn(), "arbeidsforhold.arbeidsgivernavn.label");
                pdfUtils.skrivTekstMedGuard(pdf, formaterDato(forhold.getFom(), DATO_FORMAT), "arbeidsforhold.fom.label");
                pdfUtils.skrivTekstMedGuard(pdf, formaterDato(forhold.getTom(), DATO_FORMAT), "arbeidsforhold.tom.label");

                if (forhold.getStillingsprosent() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.stillingsprosent.label") + ": " + forhold.getStillingsprosent() + "%");
                }
                pdf.addBlankLine();
            }
        } else {
            pdf.skrivTekst(getTekst("arbeidsforhold.ingen"));
            pdf.addBlankLine();
        }
        if (arbeid != null && arbeid.getKommentarTilArbeidsforhold() != null && arbeid.getKommentarTilArbeidsforhold().getVerdi() != null) {
            pdf.skrivTekst(getTekst("opplysninger.arbeidsituasjon.kommentarer.label"));
            pdf.addBlankLine();
            pdf.skrivTekstBold("Kommentar til arbeidsforhold:");
            pdf.skrivTekst(arbeid.getKommentarTilArbeidsforhold().getVerdi());
            pdf.addBlankLine();
        } else if (utvidetSoknad) {
            pdf.skrivTekst(getTekst("opplysninger.arbeidsituasjon.kommentarer.label"));
            pdf.addBlankLine();
            pdf.skrivTekstBold("Kommentar til arbeidsforhold:");
            pdfUtils.skrivIkkeUtfylt(pdf);
            pdf.addBlankLine();
        }

        pdf.skrivTekstBold(getTekst("arbeid.dinsituasjon.studerer.undertittel"));
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("dinsituasjon.studerer.sporsmal"));
        if (utdanning != null && utdanning.getErStudent() != null) {
            pdf.skrivTekst(getTekst("dinsituasjon.studerer." + utdanning.getErStudent()));

        } else {
            pdfUtils.skrivIkkeUtfylt(pdf);
        }
        pdf.addBlankLine();

        if (utvidetSoknad) {
            List<String> svaralternativer = new ArrayList<>(2);
            svaralternativer.add("dinsituasjon.studerer.true");
            svaralternativer.add("dinsituasjon.studerer.false");
            pdfUtils.skrivSvaralternativer(pdf, svaralternativer);
        }

        if (utdanning != null && utdanning.getErStudent() != null && utdanning.getErStudent()) {
            pdf.skrivTekstBold(getTekst("dinsituasjon.studerer.true.grad.sporsmal"));
            if (utdanning.getStudentgrad() != null) {
                pdf.skrivTekst(getTekst("dinsituasjon.studerer.true.grad." + utdanning.getStudentgrad()));
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }
            pdf.addBlankLine();

            if (utvidetSoknad) {
                List<String> svaralternativer = new ArrayList<>(2);
                svaralternativer.add("dinsituasjon.jobb.true.grad.deltid");
                svaralternativer.add("dinsituasjon.jobb.true.grad.heltid");
                pdfUtils.skrivSvaralternativer(pdf, svaralternativer);
            }
        }
    }

    private void leggTilFamilie(PdfGenerator pdf, JsonFamilie familie, boolean utvidetSoknad) throws IOException {

        // Familie
        pdf.skrivH4Bold(getTekst("familiebolk.tittel"));
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("familie.sivilstatus.sporsmal"));

        if (familie != null) {
            // Sivilstatus
            JsonSivilstatus sivilstatus = familie.getSivilstatus();

            if (sivilstatus != null) {
                JsonKilde kilde = sivilstatus.getKilde();

                // System
                if (kilde != null && kilde.equals(JsonKilde.SYSTEM)) {
                    JsonSivilstatus.Status status = sivilstatus.getStatus();

                    if (status == JsonSivilstatus.Status.GIFT) {
                        if (utvidetSoknad) {
                            pdf.skrivTekst(getTekst("system.familie.sivilstatus"));
                            if (sivilstatus.getEktefelleHarDiskresjonskode() != null && !sivilstatus.getEktefelleHarDiskresjonskode()) {
                                pdf.skrivTekst(getTekst("system.familie.sivilstatus.label"));
                            }
                        } else {
                            pdf.skrivTekst(getTekst("familie.sivilstatus." + status.toString()));
                        }
                        pdf.addBlankLine();

                        if (sivilstatus.getEktefelleHarDiskresjonskode() != null && sivilstatus.getEktefelleHarDiskresjonskode()) {
                            pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.ikkeTilgang.label"));
                            pdf.skrivTekst("Ektefelle/partner har diskresjonskode");
                        } else {
                            JsonEktefelle ektefelle = sivilstatus.getEktefelle();
                            if (ektefelle != null) {
                                if (!utvidetSoknad) {
                                    pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.infotekst"));
                                }
                                pdfUtils.skrivTekstMedGuard(pdf, getJsonNavnTekst(ektefelle.getNavn()), "system.familie.sivilstatus.gift.ektefelle.navn");
                                pdfUtils.skrivTekstMedGuard(pdf, formaterDato(ektefelle.getFodselsdato(), DATO_FORMAT), "system.familie.sivilstatus.gift.ektefelle.fodselsdato");

                                if (sivilstatus.getFolkeregistrertMedEktefelle() != null) {
                                    String folkeregistrertTekst = getTekst("system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen." + sivilstatus.getFolkeregistrertMedEktefelle());
                                    pdfUtils.skrivTekstMedGuard(pdf, folkeregistrertTekst, "system.familie.sivilstatus.gift.ektefelle.folkereg");
                                }
                            }
                        }
                    }
                }

                // Bruker
                if (kilde != null && kilde.equals(JsonKilde.BRUKER)) {
                    JsonSivilstatus.Status status = sivilstatus.getStatus();

                    if (status != null) {
                        pdf.skrivTekst(getTekst("familie.sivilstatus." + status.toString()));
                        pdf.addBlankLine();

                        if (utvidetSoknad) {
                            List<String> sivilstatusSvaralternativer = new ArrayList<>(5);
                            sivilstatusSvaralternativer.add("familie.sivilstatus.gift");
                            sivilstatusSvaralternativer.add("familie.sivilstatus.samboer");
                            sivilstatusSvaralternativer.add("familie.sivilstatus.separert");
                            sivilstatusSvaralternativer.add("familie.sivilstatus.skilt");
                            sivilstatusSvaralternativer.add("familie.sivilstatus.ugift");
                            pdfUtils.skrivSvaralternativer(pdf, sivilstatusSvaralternativer);
                        }

                        JsonEktefelle ektefelle = sivilstatus.getEktefelle();
                        if (ektefelle != null && status == JsonSivilstatus.Status.GIFT) {
                            pdf.skrivTekstBold(getTekst("familie.sivilstatus.gift.ektefelle.sporsmal"));

                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getNavn().getFornavn(), "familie.sivilstatus.gift.ektefelle.fornavn.label");
                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getNavn().getMellomnavn(), "familie.sivilstatus.gift.ektefelle.mellomnavn.label");
                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getNavn().getEtternavn(), "familie.sivilstatus.gift.ektefelle.etternavn.label");

                            if (ektefelle.getFodselsdato() != null) {
                                pdfUtils.skrivTekstMedGuard(pdf, formaterDato(ektefelle.getFodselsdato(), DATO_FORMAT), "familie.sivilstatus.gift.ektefelle.fnr.label");
                            } else {
                                pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.fnr.label");
                            }

                            if (ektefelle.getPersonIdentifikator() != null && ektefelle.getPersonIdentifikator().length() == 11) {
                                pdfUtils.skrivTekstMedGuard(pdf, ektefelle.getPersonIdentifikator().substring(6, 11), "familie.sivilstatus.gift.ektefelle.pnr.label");
                            } else {
                                pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.pnr.label");
                            }

                            pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getPersonIdentifikator(), "personalia.fnr");

                            if (sivilstatus.getBorSammenMed() != null) {
                                pdfUtils.skrivTekstMedGuard(pdf, getTekst("familie.sivilstatus.gift.ektefelle.borsammen." + sivilstatus.getBorSammenMed()), "familie.sivilstatus.gift.ektefelle.borsammen.sporsmal");
                            } else {
                                pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.borsammen.sporsmal");
                            }

                            if (utvidetSoknad) {
                                List<String> borSammenSvaralternativer = new ArrayList<>(2);
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.true");
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.false");
                                pdfUtils.skrivSvaralternativer(pdf, borSammenSvaralternativer);
                            }
                        }
                    } else {
                        pdfUtils.skrivIkkeUtfylt(pdf);
                    }
                }

                if (utvidetSoknad) {
                    JsonSivilstatus.Status status = sivilstatus.getStatus();
                    if (status != null) {
                        if (status.toString().equals("gift")) {
                            if (sivilstatus.getEktefelleHarDiskresjonskode() != null && !sivilstatus.getEktefelleHarDiskresjonskode()) {
                                pdf.addBlankLine();
                                pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.informasjonspanel.tittel"));
                                pdf.skrivTekst(getTekst("system.familie.sivilstatus.informasjonspanel.tekst"));
                            }
                        }
                    }
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }

            pdf.addBlankLine();
            pdf.skrivTekstBold(getTekst("familierelasjon.faktum.sporsmal"));

            // Forsørgerplikt
            JsonForsorgerplikt forsorgerplikt = familie.getForsorgerplikt();
            if (forsorgerplikt != null && forsorgerplikt.getHarForsorgerplikt() != null && Boolean.TRUE.equals(forsorgerplikt.getHarForsorgerplikt().getVerdi())) {
                if (utvidetSoknad) {
                    pdf.skrivTekst(getTekst("familierelasjon.ingress_folkeregisteret"));
                    long antallBarnFraFolkeregisteret = forsorgerplikt.getAnsvar().stream()
                            .filter(ansvar -> ansvar.getBarn().getKilde().equals(JsonKilde.SYSTEM)).count();
                    pdf.skrivTekst(getTekst("familierelasjon.ingress_forsorger") + " " + antallBarnFraFolkeregisteret + " barn under 18år");
                }

                // TODO: Finnes ikke i handlebarkode?
                //pdf.skrivTekstBold(getTekst("familie.barn.true.barn.sporsmal"));
                //pdf.addBlankLine();

                List<JsonAnsvar> listeOverAnsvar = forsorgerplikt.getAnsvar();
                leggTilBarn(pdf, utvidetSoknad, listeOverAnsvar);

                // Mottar eller betaler du barnebidrag for ett eller flere av barna?
                pdf.skrivTekstBold(getTekst("familie.barn.true.barnebidrag.sporsmal"));
                if (listeOverAnsvar.size() > 0) {

                    JsonBarnebidrag barnebidrag = forsorgerplikt.getBarnebidrag();
                    if (barnebidrag != null && barnebidrag.getVerdi() != null) {
                        JsonBarnebidrag.Verdi barnebidragVerdi = barnebidrag.getVerdi();
                        if (barnebidragVerdi != null) {
                            pdf.skrivTekst(getTekst("familie.barn.true.barnebidrag." + barnebidragVerdi.value()));
                        }

                    }
                } else {
                    pdfUtils.skrivIkkeUtfylt(pdf);

                }
                pdfUtils.skrivUtBarnebidragAlternativer(pdf, utvidetSoknad);
            } else {
                if (utvidetSoknad) {
                    pdf.skrivH3(getTekst("familierelasjon.ingen_registrerte_barn_tittel"));
                }
                pdf.skrivTekst(getTekst("familierelasjon.ingen_registrerte_barn_tekst"));
            }
        } else {
            pdfUtils.skrivIkkeUtfylt(pdf);
        }
        pdf.addBlankLine();
    }

    private void leggTilBarn(PdfGenerator pdf, boolean utvidetSoknad, List<JsonAnsvar> listeOverAnsvar) throws IOException {
        for (JsonAnsvar ansvar : listeOverAnsvar) {
            JsonBarn barn = ansvar.getBarn();
            if (barn.getKilde().equals(JsonKilde.SYSTEM) && barn.getHarDiskresjonskode() == null || !barn.getHarDiskresjonskode()) {
                // navn
                JsonNavn navnPaBarn = barn.getNavn();
                String navnPaBarnTekst = getJsonNavnTekst(navnPaBarn);
                pdfUtils.skrivTekstMedGuard(pdf, navnPaBarnTekst, "familie.barn.true.barn.navn.label");

                // Fødselsdato
                String fodselsdato = formaterDato(barn.getFodselsdato(), DATO_FORMAT);
                pdfUtils.skrivTekstMedGuard(pdf, fodselsdato, "kontakt.system.personalia.fodselsdato");

                // Samme folkeregistrerte adresse
                JsonErFolkeregistrertSammen erFolkeregistrertSammen = ansvar.getErFolkeregistrertSammen();
                if (erFolkeregistrertSammen != null) {
                    if (erFolkeregistrertSammen.getVerdi() != null && erFolkeregistrertSammen.getVerdi()) {
                        pdfUtils.skrivTekstMedGuard(pdf, "Ja", "familierelasjon.samme_folkeregistrerte_adresse");
                        leggTilDeltBosted(pdf, ansvar, true, utvidetSoknad);
                    } else {
                        pdfUtils.skrivTekstMedGuard(pdf, "Nei", "familierelasjon.samme_folkeregistrerte_adresse");
                        leggTilDeltBosted(pdf, ansvar, false, utvidetSoknad);
                    }
                }
                pdf.addBlankLine();
            }
        }
    }

    private void leggTilDeltBosted(PdfGenerator pdf, JsonAnsvar ansvar, boolean erFolkeregistrertSammenVerdi, boolean utvidetSoknad) throws IOException {
        // Har barnet delt bosted
        if (erFolkeregistrertSammenVerdi) {

            JsonHarDeltBosted harDeltBosted = ansvar.getHarDeltBosted();
            if (harDeltBosted != null) {
                pdfUtils.skrivTekstMedGuardOgIkkeUtfylt(pdf, getTekst("system.familie.barn.true.barn.deltbosted." + harDeltBosted.getVerdi()), "system.familie.barn.true.barn.deltbosted.sporsmal");
            } else {
                pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "system.familie.barn.true.barn.deltbosted.sporsmal");
            }

            if (utvidetSoknad) {
                List<String> deltBostedSvaralternativer = new ArrayList<>(2);
                deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.true");
                deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.false");
                pdfUtils.skrivSvaralternativer(pdf, deltBostedSvaralternativer);
            }

            if (utvidetSoknad) {
                pdfUtils.skrivHjelpetest(pdf, "system.familie.barn.true.barn.deltbosted.hjelpetekst.tekst");
            }
        } else {
            if (ansvar.getSamvarsgrad() != null && ansvar.getSamvarsgrad().getVerdi() != null) {
                pdfUtils.skrivTekstMedGuard(pdf, ansvar.getSamvarsgrad().getVerdi() + "%", "system.familie.barn.true.barn.grad.sporsmal");
            } else {
                pdfUtils.skrivIkkeUtfyltMedGuard(pdf, "system.familie.barn.true.barn.grad.sporsmal");
            }

            if (utvidetSoknad) {
                List<String> svaralternativer = new ArrayList<>(1);
                svaralternativer.add("system.familie.barn.true.barn.grad.pattern");
                pdfUtils.skrivSvaralternativer(pdf, svaralternativer);
            }
        }
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

    private String formaterDato(String dato, String format) {
        if (dato == null) {
            return "";
        }
        Locale locale = new Locale("nb", "NO");
        LocalDate localDate = LocalDate.parse(dato);

        return localDate.format(DateTimeFormatter.ofPattern(format, locale));
    }
}

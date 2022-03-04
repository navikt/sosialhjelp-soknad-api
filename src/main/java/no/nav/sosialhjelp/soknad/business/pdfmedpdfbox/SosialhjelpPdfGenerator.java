package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
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
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_HUSBANKEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator.INNRYKK_2;
import static no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.PdfGenerator.INNRYKK_4;
import static no.nav.sosialhjelp.soknad.pdf.InformasjonFraForsideKt.leggTilInformasjonFraForsiden;
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
            leggTilBosituasjon(pdf, data.getBosituasjon(), utvidetSoknad);
            leggTilInntektOgFormue(pdf, data.getOkonomi(), jsonInternalSoknad.getSoknad(), utvidetSoknad);
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

    private void leggTilBosituasjon(PdfGenerator pdf, JsonBosituasjon bosituasjon, boolean utvidetSoknad) throws IOException {
        pdf.skrivH4Bold(getTekst("bosituasjonbolk.tittel"));
        pdf.addBlankLine();

        if (bosituasjon != null) {
            pdf.skrivTekstBold(getTekst("bosituasjon.sporsmal"));
            JsonBosituasjon.Botype botype = bosituasjon.getBotype();
            if (botype != null) {
                String tekst = getTekst("bosituasjon." + botype.value());
                if (tekst == null || tekst.isEmpty()) {
                    pdf.skrivTekst(getTekst("bosituasjon.annet.botype." + botype.value()));
                } else {
                    pdf.skrivTekst(tekst);
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }
            pdf.addBlankLine();

            if (utvidetSoknad) {
                List<String> svaralternativer = new ArrayList<>(5);
                svaralternativer.add("bosituasjon.eier");
                svaralternativer.add("bosituasjon.kommunal");
                svaralternativer.add("bosituasjon.leier");
                svaralternativer.add("bosituasjon.ingen");
                svaralternativer.add("bosituasjon.annet");
                pdfUtils.skrivSvaralternativer(pdf, svaralternativer);

                pdf.skrivTekstBold(getTekst("bosituasjon.annet"));
                List<String> andreAlternativer = new ArrayList<>();
                andreAlternativer.add("bosituasjon.annet.botype.foreldre");
                andreAlternativer.add("bosituasjon.annet.botype.familie");
                andreAlternativer.add("bosituasjon.annet.botype.venner");
                andreAlternativer.add("bosituasjon.annet.botype.institusjon");
                andreAlternativer.add("bosituasjon.annet.botype.fengsel");
                andreAlternativer.add("bosituasjon.annet.botype.krisesenter");
                pdfUtils.skrivSvaralternativer(pdf, andreAlternativer);
            }

            pdf.skrivTekstBold(getTekst("bosituasjon.antallpersoner.sporsmal"));
            Integer antallPersoner = bosituasjon.getAntallPersoner();
            if (antallPersoner != null) {
                pdf.skrivTekst(antallPersoner.toString());
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }
        }
        pdf.addBlankLine();
    }

    private List<JsonOkonomiOpplysningUtbetaling> hentUtbetalinger(JsonOkonomi okonomi, String type) {
        return okonomi.getOpplysninger().getUtbetaling().stream()
                .filter(utbetaling -> utbetaling.getType().equals(type))
                .collect(Collectors.toList());
    }

    private List<JsonOkonomibekreftelse> hentBekreftelser(JsonOkonomi okonomi, String type) {
        if (okonomi.getOpplysninger().getBekreftelse() == null) {
            return Collections.emptyList();
        }
        return okonomi.getOpplysninger().getBekreftelse().stream()
                .filter(bekreftelse -> bekreftelse.getType().equals(type))
                .collect(Collectors.toList());
    }

    private String finnSaksStatus(JsonBostotteSak sak) {
        String status = sak.getStatus();
        if (status == null) {
            return "";
        }
        if (status.equalsIgnoreCase("VEDTATT")) {
            if (sak.getVedtaksstatus() != null && sak.getVedtaksstatus() == JsonBostotteSak.Vedtaksstatus.INNVILGET) {
                return "Innvilget: " + sak.getBeskrivelse();
            }
            if (sak.getVedtaksstatus() != null && sak.getVedtaksstatus() == JsonBostotteSak.Vedtaksstatus.AVVIST) {
                return "Avvist: " + sak.getBeskrivelse();
            }
            if (sak.getVedtaksstatus() == null) {
                return "Avslag: " + sak.getBeskrivelse();
            }
            return "Vedtatt";
        } else {
            return "Under behandling";
        }
    }

    private void leggTilInntektOgFormue(PdfGenerator pdf, JsonOkonomi okonomi, JsonSoknad soknad, boolean utvidetSoknad) throws IOException {
        pdf.skrivH4Bold(getTekst("inntektbolk.tittel"));
        pdf.addBlankLine();

        if (okonomi != null) {
            Map<String, String> urisOnPage = new HashMap<>();

            // Skatt
            pdf.skrivTekstBold(getTekst("utbetalinger.inntekt.skattbar.tittel"));
            List<JsonOkonomibekreftelse> skattetatenSamtykke = hentBekreftelser(okonomi, UTBETALING_SKATTEETATEN_SAMTYKKE);
            boolean harSkattetatenSamtykke = skattetatenSamtykke.isEmpty() ? false : skattetatenSamtykke.get(0).getVerdi();
            if (!harSkattetatenSamtykke) {
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "utbetalinger.inntekt.skattbar.samtykke_sporsmal");
                    pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.samtykke_info"));
                    pdf.addBlankLine();
                    pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.gi_samtykke"));
                    pdf.addBlankLine();
                }
                pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.mangler_samtykke"));
                pdf.addBlankLine();
            } else {
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "utbetalinger.inntekt.skattbar.samtykke_sporsmal");
                    pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.samtykke_info"));
                    pdf.addBlankLine();
                    pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.gi_samtykke"));
                    pdf.addBlankLine();
                }
                pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.har_gitt_samtykke"));
                pdf.addBlankLine();
                if (!skattetatenSamtykke.isEmpty()) {
                    pdfUtils.skrivTekstMedGuard(pdf, formaterDatoOgTidspunkt(skattetatenSamtykke.get(0).getBekreftelsesDato()), "utbetalinger.inntekt.skattbar.tidspunkt");
                }
                List<JsonOkonomiOpplysningUtbetaling> skatteetatenUtbetalinger = hentUtbetalinger(okonomi, "skatteetaten");
                if (soknad.getDriftsinformasjon() != null && soknad.getDriftsinformasjon().getInntektFraSkatteetatenFeilet()) {
                    pdf.skrivTekst("Kunne ikke hente utbetalinger fra Skatteetaten");
                    pdf.addBlankLine();
                } else {
                    if (skatteetatenUtbetalinger.isEmpty()) {
                        pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.ingen"));
                        pdf.addBlankLine();
                    }
                }
                if (!skatteetatenUtbetalinger.isEmpty()) {
                    pdf.skrivTekstBold(getTekst("utbetalinger.skatt"));
                    for (JsonOkonomiOpplysningUtbetaling skatt : skatteetatenUtbetalinger) {
                        if (skatt.getOrganisasjon() != null && skatt.getOrganisasjon().getNavn() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, skatt.getOrganisasjon().getNavn(), "utbetalinger.utbetaling.arbeidsgivernavn.label");
                        }
                        if (skatt.getPeriodeFom() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, formaterDato(skatt.getPeriodeFom(), DATO_FORMAT), "utbetalinger.utbetaling.periodeFom.label");
                        }
                        if (skatt.getPeriodeTom() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, formaterDato(skatt.getPeriodeTom(), DATO_FORMAT), "utbetalinger.utbetaling.periodeTom.label");
                        }
                        if (skatt.getBrutto() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, skatt.getBrutto().toString(), "utbetalinger.utbetaling.brutto.label");
                        }
                        if (skatt.getSkattetrekk() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, skatt.getSkattetrekk().toString(), "utbetalinger.utbetaling.skattetrekk.label");
                        }
                        pdf.addBlankLine();
                    }
                    if (utvidetSoknad) {
                        pdfUtils.skrivInfotekst(pdf, "utbetalinger.infotekst.tekst.v2");
                        urisOnPage.put("Dine Utbetalinger", getTekst("utbetalinger.infotekst.tekst.url"));
                    }
                }
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "utbetalinger.inntekt.skattbar.ta_bort_samtykke");
                }
            }

            // NAV ytelser
            pdf.skrivTekstBold(getTekst("navytelser.sporsmal"));
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "navytelser.infotekst.tekst");
            }
            if (soknad.getDriftsinformasjon() != null && soknad.getDriftsinformasjon().getUtbetalingerFraNavFeilet()) {
                pdf.skrivTekst("Kunne ikke hente utbetalinger fra NAV");
                pdf.addBlankLine();
            } else {
                List<JsonOkonomiOpplysningUtbetaling> navytelseUtbetalinger = hentUtbetalinger(okonomi, "navytelse");
                if (!navytelseUtbetalinger.isEmpty()) {
                    pdf.skrivTekstBold(getTekst("utbetalinger.sporsmal"));
                    for (JsonOkonomiOpplysningUtbetaling navytelse : navytelseUtbetalinger) {
                        pdfUtils.skrivTekstMedGuard(pdf, navytelse.getTittel(), "utbetalinger.utbetaling.type.label");
                        if (navytelse.getNetto() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, navytelse.getNetto().toString(), "utbetalinger.utbetaling.netto.label");
                        }
                        if (navytelse.getBrutto() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, navytelse.getBrutto().toString(), "utbetalinger.utbetaling.brutto.label");
                        }
                        if (navytelse.getUtbetalingsdato() != null) {
                            pdfUtils.skrivTekstMedGuard(pdf, formaterDato(navytelse.getUtbetalingsdato(), DATO_FORMAT), "utbetalinger.utbetaling.erutbetalt.label");
                        }
                    }
                    if (utvidetSoknad) {
                        pdfUtils.skrivInfotekst(pdf, "utbetalinger.infotekst.tekst.v2");
                        urisOnPage.put("Dine Utbetalinger", getTekst("utbetalinger.infotekst.tekst.url"));
                    }
                } else {
                    pdf.skrivTekst(getTekst("utbetalinger.ingen.true"));
                }
                pdf.addBlankLine();
            }

            // Bostotte
            pdf.skrivTekstBold(getTekst("inntekt.bostotte.overskrift"));
            pdf.skrivTekstBold(getTekst("inntekt.bostotte.sporsmal.sporsmal"));

            List<JsonOkonomibekreftelse> bostotteBekreftelser = hentBekreftelser(okonomi, BOSTOTTE);
            boolean motarBostotte = false;
            if (!bostotteBekreftelser.isEmpty()) {
                JsonOkonomibekreftelse bostotteBekreftelse = bostotteBekreftelser.get(0);
                motarBostotte = bostotteBekreftelse.getVerdi();
                pdf.skrivTekst(getTekst("inntekt.bostotte.sporsmal." + bostotteBekreftelse.getVerdi()));

                if (utvidetSoknad && !bostotteBekreftelse.getVerdi()) {
                    pdfUtils.skrivInfotekst(pdf, "informasjon.husbanken.bostotte.v2");
                    urisOnPage.put("støtte fra Husbanken", getTekst("informasjon.husbanken.bostotte.url"));
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                pdf.addBlankLine();
                List<String> bostotteSvaralternativer = new ArrayList<>(2);
                bostotteSvaralternativer.add("inntekt.bostotte.sporsmal.true");
                bostotteSvaralternativer.add("inntekt.bostotte.sporsmal.false");
                pdfUtils.skrivSvaralternativer(pdf, bostotteSvaralternativer);
            }
            pdf.addBlankLine();

            boolean hentingFraHusbankenHarFeilet = soknad.getDriftsinformasjon() != null && soknad.getDriftsinformasjon().getStotteFraHusbankenFeilet();
            List<JsonOkonomibekreftelse> bostotteSamtykke = hentBekreftelser(okonomi, BOSTOTTE_SAMTYKKE);
            boolean harBostotteSamtykke = bostotteSamtykke.isEmpty() ? false : bostotteSamtykke.get(0).getVerdi();
            if (harBostotteSamtykke) {
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.gi_samtykke.overskrift");
                    pdf.skrivTekst(getTekst("inntekt.bostotte.gi_samtykke.tekst"));
                    pdf.addBlankLine();
                    pdf.skrivTekst(getTekst("inntekt.bostotte.gi_samtykke"));
                    pdf.addBlankLine();
                }
                pdf.skrivTekst(getTekst("inntekt.bostotte.har_gitt_samtykke"));
                pdf.addBlankLine();
                if (!bostotteSamtykke.isEmpty()) {
                    pdfUtils.skrivTekstMedGuard(pdf, formaterDatoOgTidspunkt(bostotteSamtykke.get(0).getBekreftelsesDato()), "inntekt.bostotte.tidspunkt");
                }
                if (hentingFraHusbankenHarFeilet) {
                    pdfUtils.skrivInfotekst(pdf, "informasjon.husbanken.bostotte.nedlasting_feilet");
                }
            } else {
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.gi_samtykke.overskrift");
                    pdf.skrivTekst(getTekst("inntekt.bostotte.gi_samtykke.tekst"));
                    pdf.addBlankLine();
                    pdf.skrivTekst(getTekst("inntekt.bostotte.gi_samtykke"));
                    pdf.addBlankLine();
                }
                if (motarBostotte) {
                    pdf.skrivTekst(getTekst("inntekt.bostotte.mangler_samtykke"));
                    pdf.addBlankLine();
                }
            }

            boolean harBostotteUtbetalinger = false;
            List<JsonOkonomiOpplysningUtbetaling> husbankenUtbetalinger = hentUtbetalinger(okonomi, UTBETALING_HUSBANKEN);
            for (JsonOkonomiOpplysningUtbetaling husbanken : husbankenUtbetalinger) {
                if (husbanken.getKilde().equals(JsonKilde.SYSTEM)) {
                    if (husbanken.getMottaker() != null) {
                        pdfUtils.skrivTekstMedGuard(pdf, husbanken.getMottaker().value(), "inntekt.bostotte.utbetaling.mottaker");
                    }
                    pdfUtils.skrivTekstMedGuard(pdf, formaterDato(husbanken.getUtbetalingsdato(), DATO_FORMAT), "inntekt.bostotte.utbetaling.utbetalingsdato");
                    if (husbanken.getNetto() != null) {
                        pdfUtils.skrivTekstMedGuard(pdf, husbanken.getNetto().toString(), "inntekt.bostotte.utbetaling.belop");
                    }
                    pdf.addBlankLine();
                    harBostotteUtbetalinger = true;
                }
            }

            boolean harBostotteSaker = false;
            JsonBostotte bostotte = okonomi.getOpplysninger().getBostotte();
            if (bostotte != null && bostotte.getSaker() != null) {
                for (JsonBostotteSak bostotteSak : bostotte.getSaker()) {
                    pdf.skrivTekst(getTekst("inntekt.bostotte.sak"));
                    pdf.skrivTekst(formaterDato(bostotteSak.getDato(), DATO_FORMAT));
                    pdfUtils.skrivTekstMedGuard(pdf, finnSaksStatus(bostotteSak), "inntekt.bostotte.sak.status");
                    harBostotteSaker = true;
                }
            }

            if (harBostotteSamtykke) {
                if (harBostotteSaker) {
                    if (!harBostotteUtbetalinger) {
                        pdf.addBlankLine();
                        pdf.skrivTekst(getTekst("inntekt.bostotte.utbetalingerIkkefunnet"));
                    }
                } else {
                    if (harBostotteUtbetalinger) {
                        pdf.skrivTekst(getTekst("inntekt.bostotte.sakerIkkefunnet"));
                    } else {
                        pdf.skrivTekst(getTekst("inntekt.bostotte.ikkefunnet"));
                    }
                }
                pdf.addBlankLine();
                if (utvidetSoknad) {
                    pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.husbanken.lenkeText");
                    pdfUtils.skrivInfotekst(pdf, "inntekt.bostotte.ta_bort_samtykke");
                    urisOnPage.put(getTekst("inntekt.bostotte.husbanken.lenkeText"), getTekst("inntekt.bostotte.husbanken.url"));
                    pdf.addBlankLine();
                }
            }

            // Student
            if (soknad.getData() != null && soknad.getData().getUtdanning() != null && soknad.getData().getUtdanning().getErStudent() != null && soknad.getData().getUtdanning().getErStudent()) {
                pdf.skrivTekstBold(getTekst("inntekt.studielan.sporsmal"));

                List<JsonOkonomibekreftelse> studielanOgStipendBekreftelser = hentBekreftelser(okonomi, "studielanOgStipend");
                if (!studielanOgStipendBekreftelser.isEmpty()) {
                    JsonOkonomibekreftelse studielanOgStipendBekreftelse = studielanOgStipendBekreftelser.get(0);
                    pdf.skrivTekst(getTekst("inntekt.studielan." + studielanOgStipendBekreftelse.getVerdi()));

                    if (utvidetSoknad && !studielanOgStipendBekreftelse.getVerdi()) {
                        pdf.skrivTekstBold(getTekst("infotekst.oppsummering.tittel"));
                        pdf.skrivTekst(getTekst("informasjon.student.studielan.tittel"));
                        pdf.skrivTekst(getTekst("informasjon.student.studielan.1.v2"));
                        pdf.skrivTekst(getTekst("informasjon.student.studielan.2"));
                        urisOnPage.put("lanekassen.no", getTekst("informasjon.student.studielan.url"));
                    }
                } else {
                    pdfUtils.skrivIkkeUtfylt(pdf);
                }
                if (utvidetSoknad) {
                    List<String> studentSvaralternativer = new ArrayList<>(2);
                    studentSvaralternativer.add("inntekt.studielan.true");
                    studentSvaralternativer.add("inntekt.studielan.false");
                    pdfUtils.skrivSvaralternativer(pdf, studentSvaralternativer);
                }
                pdf.addBlankLine();
            }

            // Eierandeler
            pdf.skrivTekstBold(getTekst("inntekt.eierandeler.sporsmal"));
            if (utvidetSoknad) {
                pdfUtils.skrivHjelpetest(pdf, "inntekt.eierandeler.hjelpetekst.tekst");
            }
            List<JsonOkonomibekreftelse> verdiBekreftelser = hentBekreftelser(okonomi, "verdi");
            if (!verdiBekreftelser.isEmpty()) {
                JsonOkonomibekreftelse verdiBekreftelse = verdiBekreftelser.get(0);
                pdf.skrivTekst(getTekst("inntekt.eierandeler." + verdiBekreftelse.getVerdi()));
                List<String> verdierAlternativer = new ArrayList<>(5);
                verdierAlternativer.add("bolig");
                verdierAlternativer.add("campingvogn");
                verdierAlternativer.add("kjoretoy");
                verdierAlternativer.add("fritidseiendom");
                verdierAlternativer.add("annet");

                if (verdiBekreftelse.getVerdi()) {
                    pdf.addBlankLine();
                    pdf.skrivTekstBold(getTekst("inntekt.eierandeler.true.type.sporsmal"));
                    for (JsonOkonomioversiktFormue formue : okonomi.getOversikt().getFormue()) {
                        if (verdierAlternativer.contains(formue.getType())) {
                            pdf.skrivTekst(formue.getTittel());
                            if (formue.getType().equals("annet")) {
                                pdf.addBlankLine();
                                pdf.skrivTekstBold(getTekst("inntekt.eierandeler.true.type.annet.true.beskrivelse.label"));
                                if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null && okonomi.getOpplysninger().getBeskrivelseAvAnnet().getVerdi() != null) {
                                    pdf.skrivTekst(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getVerdi());
                                } else {
                                    pdfUtils.skrivIkkeUtfylt(pdf);
                                }
                            }
                        }
                    }
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                List<String> verdiSvaralternativer = new ArrayList<>(2);
                verdiSvaralternativer.add("inntekt.eierandeler.true");
                verdiSvaralternativer.add("inntekt.eierandeler.false");
                pdfUtils.skrivSvaralternativer(pdf, verdiSvaralternativer);

                pdf.skrivTekst("Under " + getTekst("inntekt.eierandeler.true") + ":");
                List<String> verdiJaSvaralternativer = new ArrayList<>(5);
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.bolig");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.campingvogn");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.kjoretoy");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.fritidseiendom");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.annet");
                pdfUtils.skrivSvaralternativer(pdf, verdiJaSvaralternativer);
            }
            pdf.addBlankLine();

            // Bankinnskudd
            pdf.skrivTekstBold(getTekst("inntekt.bankinnskudd.true.type.sporsmal"));
            if (utvidetSoknad) {
                pdfUtils.skrivHjelpetest(pdf, "inntekt.bankinnskudd.true.type.hjelpetekst.tekst");
            }
            List<JsonOkonomibekreftelse> sparingBekreftelser = hentBekreftelser(okonomi, "sparing");
            if (!sparingBekreftelser.isEmpty()) {
                JsonOkonomibekreftelse sparingBekreftelse = sparingBekreftelser.get(0);
                List<String> sparingAlternativer = new ArrayList<>(6);
                sparingAlternativer.add("brukskonto");
                sparingAlternativer.add("sparekonto");
                sparingAlternativer.add("bsu");
                sparingAlternativer.add("livsforsikringssparedel");
                sparingAlternativer.add("verdipapirer");
                sparingAlternativer.add("belop");

                if (sparingBekreftelse.getVerdi()) {
                    for (JsonOkonomioversiktFormue formue : okonomi.getOversikt().getFormue()) {
                        if (sparingAlternativer.contains(formue.getType())) {
                            pdf.skrivTekst(formue.getTittel());
                            if (formue.getType().equals("belop")) {
                                pdf.addBlankLine();
                                pdf.skrivTekstBold(getTekst("inntekt.bankinnskudd.true.type.annet.true.beskrivelse.label"));
                                if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null && okonomi.getOpplysninger().getBeskrivelseAvAnnet().getSparing() != null) {
                                    pdf.skrivTekst(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getSparing());
                                } else {
                                    pdfUtils.skrivIkkeUtfylt(pdf);
                                }
                            }
                        }
                    }
                } else {
                    pdfUtils.skrivIkkeUtfylt(pdf);
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                List<String> bankinnskuddSvaralternativer = new ArrayList<>(6);
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.brukskonto");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.sparekonto");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.bsu");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.livsforsikringssparedel");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.verdipapirer");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.annet");
                pdfUtils.skrivSvaralternativer(pdf, bankinnskuddSvaralternativer);
            }
            pdf.addBlankLine();

            // Inntekter
            pdf.skrivTekstBold(getTekst("inntekt.inntekter.sporsmal"));
            if (utvidetSoknad) {
                pdfUtils.skrivHjelpetest(pdf, "inntekt.inntekter.hjelpetekst.tekst");
            }
            List<JsonOkonomibekreftelse> utbetalingBekreftelser = hentBekreftelser(okonomi, "utbetaling");
            if (!utbetalingBekreftelser.isEmpty()) {
                JsonOkonomibekreftelse utbetalingBekreftelse = utbetalingBekreftelser.get(0);
                pdf.skrivTekst(getTekst("inntekt.inntekter." + utbetalingBekreftelse.getVerdi()));
                List<String> utbetalingAlternativer = new ArrayList<>(4);
                utbetalingAlternativer.add("utbytte");
                utbetalingAlternativer.add("salg");
                utbetalingAlternativer.add("forsikring");
                utbetalingAlternativer.add("annen");

                if (utbetalingBekreftelse.getVerdi()) {
                    pdf.addBlankLine();
                    pdf.skrivTekstBold(getTekst("inntekt.inntekter.true.type.sporsmal"));
                    for (JsonOkonomiOpplysningUtbetaling utbetaling : okonomi.getOpplysninger().getUtbetaling()) {
                        if (utbetalingAlternativer.contains(utbetaling.getType())) {
                            pdf.skrivTekst(utbetaling.getTittel());
                            if (utbetaling.getType().equals("annen")) {
                                pdf.addBlankLine();
                                pdf.skrivTekstBold(getTekst("inntekt.inntekter.true.type.annen.true.beskrivelse.label"));
                                if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null && okonomi.getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling() != null) {
                                    pdf.skrivTekst(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling());
                                } else {
                                    pdfUtils.skrivIkkeUtfylt(pdf);
                                }
                            }
                        }
                    }
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                List<String> inntektSvaralternativer = new ArrayList<>(2);
                inntektSvaralternativer.add("inntekt.inntekter.true");
                inntektSvaralternativer.add("inntekt.inntekter.false");
                pdfUtils.skrivSvaralternativer(pdf, inntektSvaralternativer);
                pdf.skrivTekst("Under " + getTekst("inntekt.inntekter.true") + ":");
                List<String> inntektJaSvaralternativer = new ArrayList<>(4);
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.utbytte");
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.salg");
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.forsikring");
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.annet");
                pdfUtils.skrivSvaralternativer(pdf, inntektJaSvaralternativer);
            }
            pdf.addBlankLine();

            if(urisOnPage.size() > 0) {
                pdfUtils.addLinks(pdf, urisOnPage);
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

    private String formaterDatoOgTidspunkt(String isoTimestamp) {
        if (isoTimestamp == null) {
            return "";
        }
        String format = "d. MMMM yyyy HH:mm";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(format);
        ZonedDateTime zonedDate = ZonedDateTime.parse(isoTimestamp)
                .withZoneSameInstant(ZoneId.of("Europe/Oslo"));
        return zonedDate.format(dateFormatter);
    }
}

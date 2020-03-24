package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
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
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import org.apache.commons.lang3.LocaleUtils;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.INNRYKK_1;
import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.INNRYKK_2;
import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.INNRYKK_4;
import static org.apache.cxf.common.logging.LogUtils.getLogger;

@Component
public class SosialhjelpPdfGenerator {

    private final Logger logger = getLogger(SosialhjelpPdfGenerator.class);

    private final String DATO_FORMAT = "d. MMMM yyyy";

    @Inject
    public NavMessageSource navMessageSource;

    @Inject
    public TextHelpers textHelpers;

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
            leggTilBegrunnelse(pdf, data.getBegrunnelse());
            leggTilArbeidOgUtdanning(pdf, data.getArbeid(), data.getUtdanning(), utvidetSoknad);
            leggTilFamilie(pdf, data.getFamilie(), utvidetSoknad);
            leggTilBosituasjon(pdf, data.getBosituasjon(), utvidetSoknad);
            leggTilInntektOgFormue(pdf, data.getOkonomi(), jsonInternalSoknad.getSoknad(), utvidetSoknad);
            leggTilUtgifterOgGjeld(pdf, data.getOkonomi(), jsonInternalSoknad.getSoknad(), utvidetSoknad);
            leggTilOkonomiskeOpplysningerOgVedlegg(pdf, data.getOkonomi(), jsonInternalSoknad.getVedlegg(), utvidetSoknad);
            leggTilInformasjonFraForsiden(pdf, data.getPersonalia(), utvidetSoknad);
            leggTilJuridiskInformasjon(pdf, jsonInternalSoknad.getSoknad(), utvidetSoknad);
            leggTilMetainformasjon(pdf, jsonInternalSoknad.getSoknad());

            return pdf.finish();
        } catch (IOException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }

    public void setNavMessageSource(NavMessageSource navMessageSource) {
        this.navMessageSource = navMessageSource;
    }

    public void setTextHelpers(TextHelpers textHelpers) {
        this.textHelpers = textHelpers;
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
            skrivInfotekst(pdf, "kontakt.system.personalia.infotekst.tekst");
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
            skrivHjelpetest(pdf, "soknadsmottaker.hjelpetekst.tekst");
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
                skrivInfotekst(pdf, "kontakt.system.oppholdsadresse.hvorOppholder");
            }
        }

        // Telefonnummer
        JsonTelefonnummer jsonTelefonnummer = jsonPersonalia.getTelefonnummer();
        if (jsonTelefonnummer != null) {
            if (jsonTelefonnummer.getKilde() == JsonKilde.SYSTEM) {
                pdf.skrivTekstBold(getTekst("kontakt.system.telefoninfo.sporsmal"));
                if (utvidetSoknad) {
                    skrivInfotekst(pdf, "kontakt.system.telefoninfo.infotekst.tekst");
                }
                pdf.skrivTekst(getTekst("kontakt.system.telefon.label"));
                pdf.skrivTekst(jsonTelefonnummer.getVerdi());
                pdf.addBlankLine();
                if (utvidetSoknad) {
                    skrivKnappTilgjengelig(pdf, "kontakt.system.telefon.endreknapp.label");
                }
            } else {
                pdf.skrivTekstBold(getTekst("kontakt.telefon.sporsmal"));
                pdf.skrivTekst(getTekst("kontakt.telefon.label"));
                if (jsonTelefonnummer.getVerdi() == null || jsonTelefonnummer.getVerdi().isEmpty()) {
                    skrivIkkeUtfylt(pdf);
                } else {
                    pdf.skrivTekst(jsonTelefonnummer.getVerdi());
                }
                pdf.addBlankLine();
                if (utvidetSoknad) {
                    skrivKnappTilgjengelig(pdf, "systeminfo.avbrytendringknapp.label");
                    skrivInfotekst(pdf, "kontakt.telefon.infotekst.tekst");
                }
            }
        }

        // Kontonummer
        JsonKontonummer jsonKontonummer = jsonPersonalia.getKontonummer();
        if (jsonKontonummer != null) {
            pdf.skrivTekstBold(getTekst("kontakt.kontonummer.sporsmal"));
            if (utvidetSoknad && jsonKontonummer.getKilde() == JsonKilde.SYSTEM) {
                skrivInfotekst(pdf, "kontakt.system.personalia.infotekst.tekst");
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
                    skrivIkkeUtfylt(pdf);
                } else {
                    pdf.skrivTekst(jsonKontonummer.getVerdi());
                }
            }
            pdf.addBlankLine();
            if (utvidetSoknad) {
                if (jsonKontonummer.getKilde() == JsonKilde.SYSTEM) {
                    skrivKnappTilgjengelig(pdf, "kontakt.system.kontonummer.endreknapp.label");
                } else {
                    List<String> svaralternativer = new ArrayList<>(1);
                    svaralternativer.add("kontakt.kontonummer.harikke");
                    skrivSvaralternativer(pdf, svaralternativer);
                    skrivKnappTilgjengelig(pdf, "systeminfo.avbrytendringknapp.label");
                    skrivInfotekst(pdf, "kontakt.kontonummer.infotekst.tekst");
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

    private void leggTilBegrunnelse(PdfGenerator pdf, JsonBegrunnelse jsonBegrunnelse) throws IOException {
        pdf.skrivH4Bold(getTekst("begrunnelsebolk.tittel"));
        pdf.addBlankLine();

        pdf.skrivTekstBold(getTekst("begrunnelse.hva.sporsmal"));
        if (jsonBegrunnelse.getHvaSokesOm() == null || jsonBegrunnelse.getHvaSokesOm().isEmpty()) {
            skrivIkkeUtfylt(pdf);
        } else {
            pdf.skrivTekst(jsonBegrunnelse.getHvaSokesOm());
        }
        pdf.addBlankLine();

        pdf.skrivTekstBold(getTekst("begrunnelse.hvorfor.sporsmal"));
        if (jsonBegrunnelse.getHvorforSoke() == null || jsonBegrunnelse.getHvorforSoke().isEmpty()) {
            skrivIkkeUtfylt(pdf);
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
            skrivInfotekst(pdf, "arbeidsforhold.infotekst");
        }

        if (arbeid != null && arbeid.getForhold() != null) {
            for (JsonArbeidsforhold forhold : arbeid.getForhold()) {
                skrivTekstMedGuard(pdf, forhold.getArbeidsgivernavn(), "arbeidsforhold.arbeidsgivernavn.label");
                skrivTekstMedGuard(pdf, formaterDato(forhold.getFom(), DATO_FORMAT), "arbeidsforhold.fom.label");
                skrivTekstMedGuard(pdf, formaterDato(forhold.getTom(), DATO_FORMAT), "arbeidsforhold.tom.label");

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
            skrivIkkeUtfylt(pdf);
            pdf.addBlankLine();
        }

        pdf.skrivTekstBold(getTekst("arbeid.dinsituasjon.studerer.undertittel"));
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("dinsituasjon.studerer.sporsmal"));
        if (utdanning != null && utdanning.getErStudent() != null) {
            pdf.skrivTekst(getTekst("dinsituasjon.studerer." + utdanning.getErStudent()));

        } else {
            skrivIkkeUtfylt(pdf);
        }
        pdf.addBlankLine();

        if (utvidetSoknad) {
            List<String> svaralternativer = new ArrayList<>(2);
            svaralternativer.add("dinsituasjon.studerer.true");
            svaralternativer.add("dinsituasjon.studerer.false");
            skrivSvaralternativer(pdf, svaralternativer);
        }

        if (utdanning != null && utdanning.getErStudent() != null && utdanning.getErStudent()) {
            pdf.skrivTekstBold(getTekst("dinsituasjon.studerer.true.grad.sporsmal"));
            if (utdanning.getStudentgrad() != null) {
                pdf.skrivTekst(getTekst("dinsituasjon.studerer.true.grad." + utdanning.getStudentgrad()));
            } else {
                skrivIkkeUtfylt(pdf);
            }
            pdf.addBlankLine();

            if (utvidetSoknad) {
                List<String> svaralternativer = new ArrayList<>(2);
                svaralternativer.add("dinsituasjon.jobb.true.grad.deltid");
                svaralternativer.add("dinsituasjon.jobb.true.grad.heltid");
                skrivSvaralternativer(pdf, svaralternativer);
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
                                skrivTekstMedGuard(pdf, getJsonNavnTekst(ektefelle.getNavn()), "system.familie.sivilstatus.gift.ektefelle.navn");
                                skrivTekstMedGuard(pdf, formaterDato(ektefelle.getFodselsdato(), DATO_FORMAT), "system.familie.sivilstatus.gift.ektefelle.fodselsdato");

                                if (sivilstatus.getFolkeregistrertMedEktefelle() != null) {
                                    String folkeregistrertTekst =  getTekst("system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen." + sivilstatus.getFolkeregistrertMedEktefelle());
                                    skrivTekstMedGuard(pdf, folkeregistrertTekst, "system.familie.sivilstatus.gift.ektefelle.folkereg");
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
                            skrivSvaralternativer(pdf, sivilstatusSvaralternativer);
                        }

                        JsonEktefelle ektefelle = sivilstatus.getEktefelle();
                        if (ektefelle != null && status == JsonSivilstatus.Status.GIFT) {
                            pdf.skrivTekstBold(getTekst("familie.sivilstatus.gift.ektefelle.sporsmal"));

                            skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getNavn().getFornavn(), "familie.sivilstatus.gift.ektefelle.fornavn.label");
                            skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getNavn().getMellomnavn(), "familie.sivilstatus.gift.ektefelle.mellomnavn.label");
                            skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getNavn().getEtternavn(), "familie.sivilstatus.gift.ektefelle.etternavn.label");

                            if (ektefelle.getFodselsdato() != null) {
                                skrivTekstMedGuard(pdf, formaterDato(ektefelle.getFodselsdato(), DATO_FORMAT), "familie.sivilstatus.gift.ektefelle.fnr.label");
                            } else {
                                skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.fnr.label");
                            }

                            if (ektefelle.getPersonIdentifikator() != null && ektefelle.getPersonIdentifikator().length() == 11) {
                                skrivTekstMedGuard(pdf,ektefelle.getPersonIdentifikator().substring(6,11), "familie.sivilstatus.gift.ektefelle.pnr.label");
                            } else {
                                skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.pnr.label");
                            }

                            skrivTekstMedGuardOgIkkeUtfylt(pdf, ektefelle.getPersonIdentifikator(), "personalia.fnr");

                            if (sivilstatus.getBorSammenMed() != null) {
                                skrivTekstMedGuard(pdf, getTekst("familie.sivilstatus.gift.ektefelle.borsammen." + sivilstatus.getBorSammenMed()), "familie.sivilstatus.gift.ektefelle.borsammen.sporsmal");
                            } else {
                                skrivIkkeUtfyltMedGuard(pdf, "familie.sivilstatus.gift.ektefelle.borsammen.sporsmal");
                            }

                            if (utvidetSoknad) {
                                List<String> borSammenSvaralternativer = new ArrayList<>(2);
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.true");
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.false");
                                skrivSvaralternativer(pdf, borSammenSvaralternativer);
                            }
                        }
                    } else {
                        skrivIkkeUtfylt(pdf);
                    }
                }

                if(utvidetSoknad){
                    JsonSivilstatus.Status status = sivilstatus.getStatus();
                    if(status != null){
                        if(status.toString().equals("gift")){
                            if(sivilstatus.getEktefelleHarDiskresjonskode() != null && !sivilstatus.getEktefelleHarDiskresjonskode()){
                                pdf.addBlankLine();
                                pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.informasjonspanel.tittel"));
                                pdf.skrivTekst(getTekst("system.familie.sivilstatus.informasjonspanel.tekst"));
                            }
                        }
                    }
                }
            } else {
                skrivIkkeUtfylt(pdf);
            }

            pdf.addBlankLine();

            pdf.skrivTekstBold(getTekst("familierelasjon.faktum.sporsmal"));

            // Forsørgerplikt
            JsonForsorgerplikt forsorgerplikt = familie.getForsorgerplikt();
            if (forsorgerplikt != null) {
                JsonHarForsorgerplikt harForsorgerplikt = forsorgerplikt.getHarForsorgerplikt();
                if (harForsorgerplikt != null && harForsorgerplikt.getVerdi()) {

                    if (utvidetSoknad) {
                        pdf.skrivTekst(getTekst("familierelasjon.ingress_folkeregisteret"));
                        pdf.skrivTekst(getTekst("familierelasjon.ingress_forsorger") + " " + forsorgerplikt.getAnsvar().size() + " barn under 18år");
                    }

                    // TODO: Finnes ikke i handlebarkode?
                    //pdf.skrivTekstBold(getTekst("familie.barn.true.barn.sporsmal"));
                    //pdf.addBlankLine();

                    List<JsonAnsvar> listeOverAnsvar = forsorgerplikt.getAnsvar();

                    for (JsonAnsvar ansvar : listeOverAnsvar) {
                        JsonBarn barn = ansvar.getBarn();

                        if (barn.getHarDiskresjonskode() == null || !barn.getHarDiskresjonskode()) {
                            // navn
                            JsonNavn navnPaBarn = barn.getNavn();
                            String navnPaBarnTekst = getJsonNavnTekst(navnPaBarn);
                            skrivTekstMedGuard(pdf, navnPaBarnTekst, "familie.barn.true.barn.navn.label");

                            // Fødselsdato
                            String fodselsdato = formaterDato(barn.getFodselsdato(), DATO_FORMAT);
                            skrivTekstMedGuard(pdf, fodselsdato, "kontakt.system.personalia.fnr");

                            // Personnummer TODO: Finnes ikke i søknad eller handlebarkode?
                            //String personIdentifikator = barn.getPersonIdentifikator();
                            // skrivTekstMedGuard(pdf, personIdentifikator, "kontakt.system.personalia.fnr");

                            // Samme folkeregistrerte adresse
                            JsonErFolkeregistrertSammen erFolkeregistrertSammen = ansvar.getErFolkeregistrertSammen();
                            if (erFolkeregistrertSammen != null) {
                                if (erFolkeregistrertSammen.getVerdi() != null && erFolkeregistrertSammen.getVerdi()) {
                                    skrivTekstMedGuard(pdf, "Ja", "familierelasjon.samme_folkeregistrerte_adresse");
                                    leggTilDeltBosted(pdf, ansvar, true, utvidetSoknad);
                                } else {
                                    skrivTekstMedGuard(pdf, "Nei", "familierelasjon.samme_folkeregistrerte_adresse");
                                    leggTilDeltBosted(pdf, ansvar, false, utvidetSoknad);
                                }
                            }
                            pdf.addBlankLine();
                        }
                    }

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
                        skrivIkkeUtfylt(pdf);

                    }
                    skrivUtBarnebidragAlternativer(pdf, utvidetSoknad);
                }
            }
            else {
                if (utvidetSoknad) {
                    pdf.skrivH3(getTekst("familierelasjon.ingen_registerte_barn_tittel"));
                    pdf.skrivTekst(getTekst("familierelasjon.ingen_registrerte_barn_tekst"));
                }
            }
        } else {
            skrivIkkeUtfylt(pdf);
        }
        pdf.addBlankLine();
    }

    private void leggTilDeltBosted(PdfGenerator pdf, JsonAnsvar ansvar, Boolean erFolkeregistrertSammenVerdi, boolean utvidetSoknad) throws IOException {
        // Har barnet delt bosted
        if (erFolkeregistrertSammenVerdi) {

            JsonHarDeltBosted harDeltBosted = ansvar.getHarDeltBosted();
            skrivTekstMedGuardOgIkkeUtfylt(pdf, getTekst("system.familie.barn.true.barn.deltbosted." + harDeltBosted.getVerdi()), "system.familie.barn.true.barn.deltbosted.sporsmal");

            if (utvidetSoknad) {
                List<String> deltBostedSvaralternativer = new ArrayList<>(2);
                deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.true");
                deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.false");
                skrivSvaralternativer(pdf, deltBostedSvaralternativer);
            }

            if(utvidetSoknad){
                skrivHjelpetest(pdf, "system.familie.barn.true.barn.deltbosted.hjelpetekst.tekst");
            }
        } else {
            if (ansvar.getSamvarsgrad() != null && ansvar.getSamvarsgrad().getVerdi() != null ) {
                skrivTekstMedGuard(pdf, ansvar.getSamvarsgrad().getVerdi() + "%", "system.familie.barn.true.barn.grad.sporsmal");
            } else {
                skrivIkkeUtfyltMedGuard(pdf,  "system.familie.barn.true.barn.grad.sporsmal");
            }

            if (utvidetSoknad) {
                List<String> svaralternativer = new ArrayList<>(1);
                svaralternativer.add("system.familie.barn.true.barn.grad.pattern");
                skrivSvaralternativer(pdf, svaralternativer);
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
                skrivIkkeUtfylt(pdf);
            }
            pdf.addBlankLine();

            if (utvidetSoknad) {
                List<String> svaralternativer = new ArrayList<>(5);
                svaralternativer.add("bosituasjon.eier");
                svaralternativer.add("bosituasjon.kommunal");
                svaralternativer.add("bosituasjon.leier");
                svaralternativer.add("bosituasjon.ingen");
                svaralternativer.add("bosituasjon.annet");
                skrivSvaralternativer(pdf, svaralternativer);

                pdf.skrivTekstBold(getTekst("bosituasjon.annet"));
                List<String> andreAlternativer = new ArrayList<>();
                andreAlternativer.add("bosituasjon.annet.botype.foreldre");
                andreAlternativer.add("bosituasjon.annet.botype.familie");
                andreAlternativer.add("bosituasjon.annet.botype.venner");
                andreAlternativer.add("bosituasjon.annet.botype.institusjon");
                andreAlternativer.add("bosituasjon.annet.botype.fengsel");
                andreAlternativer.add("bosituasjon.annet.botype.krisesenter");
                skrivSvaralternativer(pdf, andreAlternativer);
            }

            pdf.skrivTekstBold(getTekst("bosituasjon.antallpersoner.sporsmal"));
            Integer antallPersoner = bosituasjon.getAntallPersoner();
            if (antallPersoner != null) {
                pdf.skrivTekst(antallPersoner.toString());
            } else {
                skrivIkkeUtfylt(pdf);
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
            // Skatt
            pdf.skrivTekstBold(getTekst("utbetalinger.inntekt.skattbar.tittel"));
            if (utvidetSoknad) {
                skrivInfotekst(pdf, "utbetalinger.inntekt.skattbar.beskrivelse");
            }
            if (soknad.getDriftsinformasjon() != null && soknad.getDriftsinformasjon().getInntektFraSkatteetatenFeilet()) {
                pdf.skrivTekst("Kunne ikke hente utbetalinger fra Skatteetaten");
                pdf.addBlankLine();
            }
            else {
                List<JsonOkonomiOpplysningUtbetaling> skatteetatenUtbetalinger = hentUtbetalinger(okonomi, "skatteetaten");
                if (!skatteetatenUtbetalinger.isEmpty()) {
                    pdf.skrivTekstBold(getTekst("utbetalinger.skatt"));
                    for (JsonOkonomiOpplysningUtbetaling skatt : skatteetatenUtbetalinger) {
                        if (skatt.getOrganisasjon() != null && skatt.getOrganisasjon().getNavn() != null) {
                            skrivTekstMedGuard(pdf, skatt.getOrganisasjon().getNavn(), "utbetalinger.utbetaling.arbeidsgivernavn.label");
                        }
                        if (skatt.getPeriodeFom() != null) {
                            skrivTekstMedGuard(pdf, formaterDato(skatt.getPeriodeFom(), DATO_FORMAT), "utbetalinger.utbetaling.periodeFom.label");
                        }
                        if (skatt.getPeriodeTom() != null) {
                            skrivTekstMedGuard(pdf, formaterDato(skatt.getPeriodeTom(), DATO_FORMAT), "utbetalinger.utbetaling.periodeTom.label");
                        }
                        if (skatt.getBrutto() != null) {
                            skrivTekstMedGuard(pdf, skatt.getBrutto().toString(), "utbetalinger.utbetaling.brutto.label");
                        }
                        if (skatt.getSkattetrekk() != null) {
                            skrivTekstMedGuard(pdf, skatt.getSkattetrekk().toString(), "utbetalinger.utbetaling.skattetrekk.label");
                        }
                        pdf.addBlankLine();
                    }
                    if (utvidetSoknad) {
                        skrivInfotekst(pdf, "utbetalinger.infotekst.tekst");
                    }
                } else {
                    pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.ingen"));
                    pdf.addBlankLine();
                }
            }

            // NAV ytelser
            pdf.skrivTekstBold(getTekst("navytelser.sporsmal"));
            if (utvidetSoknad) {
                skrivInfotekst(pdf, "navytelser.infotekst.tekst");
            }
            if (soknad.getDriftsinformasjon() != null && soknad.getDriftsinformasjon().getUtbetalingerFraNavFeilet()) {
                pdf.skrivTekst("Kunne ikke hente utbetalinger fra NAV");
                pdf.addBlankLine();
            } else {
                List<JsonOkonomiOpplysningUtbetaling> navytelseUtbetalinger = hentUtbetalinger(okonomi, "navytelse");
                if (!navytelseUtbetalinger.isEmpty()) {
                    pdf.skrivTekstBold(getTekst("utbetalinger.sporsmal"));
                    for (JsonOkonomiOpplysningUtbetaling navytelse : navytelseUtbetalinger) {
                        skrivTekstMedGuard(pdf, navytelse.getTittel(), "utbetalinger.utbetaling.type.label");
                        if (navytelse.getNetto() != null) {
                            skrivTekstMedGuard(pdf, navytelse.getNetto().toString(), "utbetalinger.utbetaling.netto.label");
                        }
                        if (navytelse.getBrutto() != null) {
                            skrivTekstMedGuard(pdf, navytelse.getBrutto().toString(), "utbetalinger.utbetaling.brutto.label");
                        }
                        if (navytelse.getUtbetalingsdato() != null) {
                            skrivTekstMedGuard(pdf, formaterDato(navytelse.getUtbetalingsdato(), DATO_FORMAT), "utbetalinger.utbetaling.erutbetalt.label");
                        }
                    }
                    if (utvidetSoknad) {
                        skrivInfotekst(pdf, "utbetalinger.infotekst.tekst");
                    }
                } else {
                    pdf.skrivTekst(getTekst("utbetalinger.ingen.true"));
                }
                pdf.addBlankLine();
            }

            // Student
            if (soknad.getData() != null && soknad.getData().getUtdanning() != null && soknad.getData().getUtdanning().getErStudent() != null) {
                pdf.skrivTekstBold(getTekst("inntekt.studielan.sporsmal"));

                List<JsonOkonomibekreftelse> studielanOgStipendBekreftelser = hentBekreftelser(okonomi, "studielanOgStipend");
                if (!studielanOgStipendBekreftelser.isEmpty()) {
                    JsonOkonomibekreftelse studielanOgStipendBekreftelse = studielanOgStipendBekreftelser.get(0);
                    pdf.skrivTekst(getTekst("inntekt.studielan." + studielanOgStipendBekreftelse.getVerdi()));

                    if (utvidetSoknad && !studielanOgStipendBekreftelse.getVerdi()) {
                        pdf.skrivTekstBold(getTekst("infotekst.oppsummering.tittel"));
                        pdf.skrivTekst(getTekst("informasjon.student.studielan.tittel"));
                        pdf.skrivTekst(getTekst("informasjon.student.studielan.1"));
                        pdf.skrivTekst(getTekst("informasjon.student.studielan.2"));
                    }
                } else {
                    skrivIkkeUtfylt(pdf);
                }
                if (utvidetSoknad) {
                    List<String> studentSvaralternativer = new ArrayList<>(2);
                    studentSvaralternativer.add("inntekt.studielan.true");
                    studentSvaralternativer.add("inntekt.studielan.false");
                    skrivSvaralternativer(pdf, studentSvaralternativer);
                }
                pdf.addBlankLine();
            }

            // Bostotte
            pdf.skrivTekstBold(getTekst("inntekt.bostotte.overskrift"));

            if (soknad.getDriftsinformasjon() != null && soknad.getDriftsinformasjon().getStotteFraHusbankenFeilet()) {
                pdf.skrivTekstBold(getTekst("inntekt.bostotte.sporsmal.sporsmal"));

                List<JsonOkonomibekreftelse> bostotteBekreftelser = hentBekreftelser(okonomi, "bostotte");
                if (!bostotteBekreftelser.isEmpty()) {
                    JsonOkonomibekreftelse bostotteBekreftelse = bostotteBekreftelser.get(0);
                    pdf.skrivTekst(getTekst("inntekt.bostotte.sporsmal." + bostotteBekreftelse.getVerdi()));

                    if (utvidetSoknad && !bostotteBekreftelse.getVerdi()) {
                        skrivInfotekst(pdf, "informasjon.husbanken.bostotte");
                    }
                } else {
                    skrivIkkeUtfylt(pdf);
                }
                if (utvidetSoknad) {
                    List<String> bostotteSvaralternativer = new ArrayList<>(2);
                    bostotteSvaralternativer.add("inntekt.bostotte.sporsmal.true");
                    bostotteSvaralternativer.add("inntekt.bostotte.sporsmal.false");
                    skrivSvaralternativer(pdf, bostotteSvaralternativer);
                }
                pdf.addBlankLine();
            } else {
                if (utvidetSoknad) {
                    skrivInfotekst(pdf, "inntekt.bostotte.infotekst.tekst");
                }
                List<JsonOkonomiOpplysningUtbetaling> husbankenUtbetalinger = hentUtbetalinger(okonomi, "husbanken");
                for (JsonOkonomiOpplysningUtbetaling husbanken : husbankenUtbetalinger) {
                    if (husbanken.getMottaker() != null) {
                        skrivTekstMedGuard(pdf, husbanken.getMottaker().value(), "inntekt.bostotte.utbetaling.mottaker");
                    }
                    skrivTekstMedGuard(pdf, formaterDato(husbanken.getUtbetalingsdato(), DATO_FORMAT), "inntekt.bostotte.utbetaling.utbetalingsdato");
                    if (husbanken.getNetto() != null) {
                        skrivTekstMedGuard(pdf, husbanken.getNetto().toString(), "inntekt.bostotte.utbetaling.belop");
                    }
                    pdf.addBlankLine();
                }
                JsonBostotte bostotte = okonomi.getOpplysninger().getBostotte();
                if (bostotte != null && bostotte.getSaker() != null) {
                    for (JsonBostotteSak bostotteSak : bostotte.getSaker()) {
                        pdf.skrivTekst(getTekst("inntekt.bostotte.sak"));
                        pdf.skrivTekst(formaterDato(bostotteSak.getDato(), DATO_FORMAT));
                        skrivTekstMedGuard(pdf, finnSaksStatus(bostotteSak), "inntekt.bostotte.sak.status");
                    }
                    boolean utbetalingHusbankenFinnes = hentUtbetalinger(okonomi, "bostotte").size() > 0;
                    if (bostotte.getSaker().isEmpty()) {

                        if (utbetalingHusbankenFinnes) {
                            pdf.skrivTekst(getTekst("inntekt.bostotte.sakerIkkefunnet"));
                        } else {
                            pdf.skrivTekst(getTekst("inntekt.bostotte.ikkefunnet"));
                        }
                    } else {
                        if (!utbetalingHusbankenFinnes) {
                            pdf.skrivTekst(getTekst("inntekt.bostotte.utbetalingerIkkefunnet"));
                        }
                    }
                    pdf.addBlankLine();
                }
            }

            // Eierandeler
            pdf.skrivTekstBold(getTekst("inntekt.eierandeler.sporsmal"));
            if (utvidetSoknad) {
                skrivHjelpetest(pdf, "inntekt.eierandeler.hjelpetekst.tekst");
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
                                    skrivIkkeUtfylt(pdf);
                                }
                            }
                        }
                    }
                }
            } else {
                skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                List<String> verdiSvaralternativer = new ArrayList<>(2);
                verdiSvaralternativer.add("inntekt.eierandeler.true");
                verdiSvaralternativer.add("inntekt.eierandeler.false");
                skrivSvaralternativer(pdf, verdiSvaralternativer);

                pdf.skrivTekst("Under " + getTekst("inntekt.eierandeler.true") + ":");
                List<String> verdiJaSvaralternativer = new ArrayList<>(5);
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.bolig");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.campingvogn");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.kjoretoy");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.fritidseiendom");
                verdiJaSvaralternativer.add("inntekt.eierandeler.true.type.annet");
                skrivSvaralternativer(pdf, verdiJaSvaralternativer);
            }
            pdf.addBlankLine();

            // Bankinnskudd
            pdf.skrivTekstBold(getTekst("inntekt.bankinnskudd.true.type.sporsmal"));
            if (utvidetSoknad) {
                skrivHjelpetest(pdf, "inntekt.bankinnskudd.true.type.hjelpetekst.tekst");
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
                                    skrivIkkeUtfylt(pdf);
                                }
                            }
                        }
                    }
                } else {
                    skrivIkkeUtfylt(pdf);
                }
            } else {
                skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                List<String> bankinnskuddSvaralternativer = new ArrayList<>(6);
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.brukskonto");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.sparekonto");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.bsu");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.livsforsikringssparedel");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.verdipapirer");
                bankinnskuddSvaralternativer.add("inntekt.bankinnskudd.true.type.annet");
            }
            pdf.addBlankLine();

            // Inntekter
            pdf.skrivTekstBold(getTekst("inntekt.inntekter.sporsmal"));
            if (utvidetSoknad) {
                skrivHjelpetest(pdf, "inntekt.inntekter.hjelpetekst.tekst");
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
                                pdf.skrivTekstBold(getTekst("inntekt.eierandeler.true.type.annet.true.beskrivelse.label"));
                                if (okonomi.getOpplysninger().getBeskrivelseAvAnnet() != null && okonomi.getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling() != null) {
                                    pdf.skrivTekst(okonomi.getOpplysninger().getBeskrivelseAvAnnet().getUtbetaling());
                                } else {
                                    skrivIkkeUtfylt(pdf);
                                }
                            }
                        }
                    }
                }
            } else {
                skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                List<String> inntektSvaralternativer = new ArrayList<>(2);
                inntektSvaralternativer.add("inntekt.inntekter.true");
                inntektSvaralternativer.add("inntekt.inntekter.false");
                skrivSvaralternativer(pdf, inntektSvaralternativer);
                pdf.skrivTekst("Under " + getTekst("inntekt.inntekter.true") + ":");
                List<String> inntektJaSvaralternativer = new ArrayList<>(4);
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.utbytte");
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.salg");
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.forsikring");
                inntektJaSvaralternativer.add("inntekt.inntekter.true.type.annet");
                skrivSvaralternativer(pdf, inntektJaSvaralternativer);
            }
            pdf.addBlankLine();
        }
    }

    private void leggTilUtgifterOgGjeld(PdfGenerator pdf, JsonOkonomi okonomi, JsonSoknad soknad, boolean utvidetSoknad) throws IOException {
        pdf.skrivH4Bold(getTekst("utgifterbolk.tittel"));
        pdf.addBlankLine();

        if (okonomi != null) {
            // Boutgifter
            pdf.skrivTekstBold(getTekst("utgifter.boutgift.sporsmal"));
            if (utvidetSoknad) {
                skrivInfotekst(pdf, "utgifter.boutgift.infotekst.tekst");
            }
            List<JsonOkonomibekreftelse> boutgifterBekreftelser = hentBekreftelser(okonomi, "boutgifter");
            if (!boutgifterBekreftelser.isEmpty()) {
                JsonOkonomibekreftelse boutgifterBekreftelse = boutgifterBekreftelser.get(0);

                pdf.skrivTekst(getTekst("utgifter.boutgift." + boutgifterBekreftelse.getVerdi()));
                if (boutgifterBekreftelse.getVerdi()) {
                    pdf.addBlankLine();
                    pdf.skrivTekstBold(getTekst("utgifter.boutgift.true.type.sporsmal"));

                    List<String> boutgiftAlternativer = new ArrayList<>(6);
                    boutgiftAlternativer.add("husleie");
                    boutgiftAlternativer.add("strom");
                    boutgiftAlternativer.add("kommunalAvgift");
                    boutgiftAlternativer.add("oppvarming");
                    boutgiftAlternativer.add("boliglanAvdrag"); // boliglanRenter er ikke tatt med her, da det kun er ett valg for disse i frontend
                    boutgiftAlternativer.add("annenBoutgift");

                    for (JsonOkonomiOpplysningUtgift opplysningUtgift : okonomi.getOpplysninger().getUtgift()) {
                        if (boutgiftAlternativer.contains(opplysningUtgift.getType())) {
                            pdf.skrivTekst(getTekst("utgifter.boutgift.true.type." + opplysningUtgift.getType()));
                        }
                    }
                    for (JsonOkonomioversiktUtgift oversiktUtgift : okonomi.getOversikt().getUtgift()) {
                        if (boutgiftAlternativer.contains(oversiktUtgift.getType())) {
                            pdf.skrivTekst(getTekst("utgifter.boutgift.true.type." + oversiktUtgift.getType()));
                        }
                    }
                }
            } else {
                skrivIkkeUtfylt(pdf);
            }
            if (utvidetSoknad) {
                List<String> boutgifterSvaralternativer = new ArrayList<>(2);
                boutgifterSvaralternativer.add("utgifter.boutgift.true");
                boutgifterSvaralternativer.add("utgifter.boutgift.false");
                skrivSvaralternativer(pdf, boutgifterSvaralternativer);
                pdf.skrivTekst("Under: " + getTekst("utgifter.boutgift.true"));
                List<String> boutgifterJaSvaralternativer = new ArrayList<>();
                boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.husleie");
                boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.strom");
                boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.kommunalAvgift");
                boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.oppvarming");
                boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.boliglanAvdrag");
                boutgifterJaSvaralternativer.add("utgifter.boutgift.true.type.annenBoutgift");
                skrivSvaralternativer(pdf, boutgifterJaSvaralternativer);
            }
            pdf.addBlankLine();

            // Forsørgerplikt
            if (soknad.getData().getFamilie() != null && soknad.getData().getFamilie().getForsorgerplikt() != null && soknad.getData().getFamilie().getForsorgerplikt().getHarForsorgerplikt() != null) {
                pdf.skrivTekstBold(getTekst("utgifter.barn.sporsmal"));
                if (utvidetSoknad) {
                    skrivInfotekst(pdf, "utgifter.barn.infotekst.tekst");
                }
                List<JsonOkonomibekreftelse> barneutgifterBekreftelser = hentBekreftelser(okonomi, "barneutgifter");
                if (!barneutgifterBekreftelser.isEmpty()) {
                    JsonOkonomibekreftelse barneutgiftBekreftelse = barneutgifterBekreftelser.get(0);

                    pdf.skrivTekst(getTekst("utgifter.barn." + barneutgiftBekreftelse.getVerdi()));

                    if (barneutgiftBekreftelse.getVerdi()) {
                        pdf.addBlankLine();
                        pdf.skrivTekstBold(getTekst("utgifter.barn.true.utgifter.sporsmal"));

                        List<String> utgifterBarnAlternativer = new ArrayList<>(5);
                        utgifterBarnAlternativer.add("barnFritidsaktiviteter");
                        utgifterBarnAlternativer.add("barnehage");
                        utgifterBarnAlternativer.add("sfo");
                        utgifterBarnAlternativer.add("barnTannregulering");
                        utgifterBarnAlternativer.add("annenBarneutgift");

                        for (JsonOkonomiOpplysningUtgift opplysningUtgift : okonomi.getOpplysninger().getUtgift()) {
                            if (utgifterBarnAlternativer.contains(opplysningUtgift.getType())) {
                                pdf.skrivTekst(getTekst("utgifter.barn.true.utgifter." + opplysningUtgift.getType()));
                            }
                        }
                        for (JsonOkonomioversiktUtgift oversiktUtgift : okonomi.getOversikt().getUtgift()) {
                            if (utgifterBarnAlternativer.contains(oversiktUtgift.getType())) {
                                pdf.skrivTekst(getTekst("utgifter.barn.true.utgifter." + oversiktUtgift.getType()));
                            }
                        }
                    }

                } else {
                    skrivIkkeUtfylt(pdf);
                }

                pdf.addBlankLine();

                if (utvidetSoknad) {
                    List<String> utgifterBarnSvaralternativer = new ArrayList<>(2);
                    utgifterBarnSvaralternativer.add("utgifter.barn.true");
                    utgifterBarnSvaralternativer.add("utgifter.barn.false");
                    skrivSvaralternativer(pdf, utgifterBarnSvaralternativer);
                    pdf.skrivTekst("Under: " + getTekst("utgifter.barn.true"));
                    List<String> utgifterBarnJaSvaralternativer = new ArrayList<>(5);
                    utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.barnFritidsaktiviteter"); // Fritid
                    utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.barnehage"); // Barnehage
                    utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.sfo"); // SFO
                    utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.barnTannregulering"); // Regulering
                    utgifterBarnJaSvaralternativer.add("utgifter.barn.true.utgifter.annenBarneutgift"); // Annet
                    skrivSvaralternativer(pdf, utgifterBarnJaSvaralternativer);
                }
            }
        }
    }

    private void leggTilOkonomiskeOpplysningerOgVedlegg(PdfGenerator pdf, JsonOkonomi okonomi, JsonVedleggSpesifikasjon vedleggSpesifikasjon, boolean utvidetSoknad) throws IOException {
        List<String> utgifterBarnAlternativer = new ArrayList<>(5);
        utgifterBarnAlternativer.add("barnFritidsaktiviteter");
        utgifterBarnAlternativer.add("barnehage");
        utgifterBarnAlternativer.add("sfo");
        utgifterBarnAlternativer.add("barnTannregulering");
        utgifterBarnAlternativer.add("annenBarneutgift");

        List<String> boutgiftAlternativer = new ArrayList<>(7);
        boutgiftAlternativer.add("husleie");
        boutgiftAlternativer.add("strom");
        boutgiftAlternativer.add("kommunalAvgift");
        boutgiftAlternativer.add("oppvarming");
        boutgiftAlternativer.add("boliglanAvdrag");
        boutgiftAlternativer.add("boliglanRenter");
        boutgiftAlternativer.add("annenBoutgift");

        pdf.skrivH4Bold(getTekst("opplysningerbolk.tittel"));
        pdf.addBlankLine();

        if (utvidetSoknad) {
            if (okonomi.getOpplysninger().getBekreftelse() != null && !okonomi.getOpplysninger().getBekreftelse().isEmpty()) {
                skrivInfotekst(pdf, "opplysninger.informasjon");
            } else {
                skrivInfotekst(pdf, "opplysninger.ikkebesvart.melding");
            }
        }

        // Inntekt
        pdf.skrivTekstBold(getTekst("inntektbolk.tittel"));
        // Kan ikke være null i filformatet
        for (JsonOkonomioversiktInntekt inntekt : okonomi.getOversikt().getInntekt()) {
            pdf.skrivTekst(inntekt.getTittel());
            if (inntekt.getType().equals("bostotte")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, inntekt.getNetto(), "opplysninger.inntekt.bostotte.utbetaling.label");
            }
            if (inntekt.getType().equals("studielanOgStipend")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, inntekt.getNetto(), "opplysninger.arbeid.student.utbetaling.label");
            }
            if (inntekt.getType().equals("jobb")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, inntekt.getBrutto(), "opplysninger.arbeid.jobb.bruttolonn.label");
                skrivTekstMedGuardOgIkkeUtfylt(pdf, inntekt.getNetto(), "opplysninger.arbeid.jobb.nettolonn.label");
            }
            if (inntekt.getType().equals("barnebidrag")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, inntekt.getNetto(), "opplysninger.familiesituasjon.barnebidrag.mottar.mottar.label");
            }
            pdf.addBlankLine();
        }

        // Formue
        List<String> sparingTyper = new ArrayList<>(6);
        sparingTyper.add("verdipapirer");
        sparingTyper.add("brukskonto");
        sparingTyper.add("bsu");
        sparingTyper.add("livsforsikringssparedel");
        sparingTyper.add("sparekonto");
        sparingTyper.add("belop");
        for (JsonOkonomioversiktFormue formue : okonomi.getOversikt().getFormue()) {
            if (sparingTyper.contains(formue.getType())) {
                pdf.skrivTekst(formue.getTittel());
                skrivTekstMedGuardOgIkkeUtfylt(pdf, formue.getBelop(), "opplysninger.inntekt.bankinnskudd." + formue.getType() + ".saldo.label");
                pdf.addBlankLine();
            }
        }

        // Utbetaling
        for (JsonOkonomiOpplysningUtbetaling utbetaling : okonomi.getOpplysninger().getUtbetaling()) {
            if (!utbetaling.getType().equals("skatteetaten") && !utbetaling.getType().equals("navytelse") && !utbetaling.getType().equals("husbanken")) {
                pdf.skrivTekst(utbetaling.getTittel());
                if (utbetaling.getType().equals("sluttoppgjoer")) {
                    skrivTekstMedGuardOgIkkeUtfylt(pdf, utbetaling.getBelop(), "opplysninger.arbeid.avsluttet.netto.label");
                } else {
                    if (getTekst("opplysninger.inntekt.inntekter." + utbetaling.getType() + ".sum.label") == null) {
                        pdf.skrivTekstBold("MANGLER OVERSETTELSE: " + utbetaling.getType());
                    }
                    skrivTekstMedGuardOgIkkeUtfylt(pdf, utbetaling.getBelop(), "opplysninger.inntekt.inntekter." + utbetaling.getType() + ".sum.label");
                }
                pdf.addBlankLine();
            }
        }

        // Utgift
        pdf.skrivTekstBold(getTekst("utgifterbolk.tittel"));
        for (JsonOkonomiOpplysningUtgift utgift : okonomi.getOpplysninger().getUtgift()) {
            pdf.skrivTekst(utgift.getTittel());

            if (utgifterBarnAlternativer.contains(utgift.getType())) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.utgifter.barn." + utgift.getType() + ".sisteregning.label");
            }
            if (boutgiftAlternativer.contains(utgift.getType())) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.utgifter.boutgift." + utgift.getType() + ".sisteregning.label");
            }
            if (utgift.getType().equals("annen")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.ekstrainfo.utgifter.utgift.label");
            }
            pdf.addBlankLine();
        }
        for (JsonOkonomioversiktUtgift utgift : okonomi.getOversikt().getUtgift()) {
            pdf.skrivTekst(utgift.getTittel());

            if (utgifterBarnAlternativer.contains(utgift.getType())) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.utgifter.barn." + utgift.getType() + ".sistemnd.label");
            }
            if (utgift.getType().equals("barnebidrag")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.familiesituasjon.barnebidrag.betaler.betaler.label");
            }
            if (utgift.getType().equals("husleie")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.utgifter.boutgift.husleie.permnd.label");
            }
            if (utgift.getType().equals("boliglanAvdrag")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.utgifter.boutgift.avdraglaan.avdrag.label");
            }
            if (utgift.getType().equals("boliglanRenter")) {
                skrivTekstMedGuardOgIkkeUtfylt(pdf, utgift.getBelop(), "opplysninger.utgifter.boutgift.avdraglaan.renter.label");
            }
            pdf.addBlankLine();
        }

        // Vedlegg
        pdf.skrivTekstBold(getTekst("vedlegg.oppsummering.tittel"));
        if (vedleggSpesifikasjon != null && vedleggSpesifikasjon.getVedlegg() != null) {
            for (JsonVedlegg vedlegg : vedleggSpesifikasjon.getVedlegg()) {
                pdf.skrivTekst(getTekst("vedlegg." + vedlegg.getType() + "." + vedlegg.getTilleggsinfo() + ".tittel"));
                if (vedlegg.getFiler() != null) {
                    for (JsonFiler fil : vedlegg.getFiler()) {
                        pdf.skrivTekst(" - " + fil.getFilnavn());
                    }
                }
                if (vedlegg.getStatus() != null && vedlegg.getStatus().equals("VedleggKreves")) {
                    pdf.skrivTekst(getTekst("vedlegg.oppsummering.ikkelastetopp"));
                }
                if (vedlegg.getStatus() != null && vedlegg.getStatus().equals("VedleggAlleredeSendt")) {
                    pdf.skrivTekst(getTekst("opplysninger.vedlegg.alleredelastetopp"));
                }
                pdf.addBlankLine();
            }
        }
    }

    private void leggTilInformasjonFraForsiden(PdfGenerator pdf, JsonPersonalia personalia, boolean utvidetSoknad) throws IOException {
        if (utvidetSoknad) {
            pdf.skrivH4Bold(getTekst("informasjon.tittel"));
            pdf.addBlankLine();
            pdf.skrivTekst("Hei " + personalia.getNavn().getFornavn());
            pdf.skrivTekst(getTekst("informasjon.hilsen.tittel"));
            pdf.addBlankLine();
            pdf.skrivTekstBold(getTekst("informasjon.start.undertittel"));
            pdf.skrivTekst("Før du søker bør du undersøke andre muligheter til å forsørge deg selv. Les mer om andre muligheter.");
            pdf.addBlankLine();
            pdf.skrivTekst("Du må i utgangspunktet ha lovlig opphold og fast bopel i Norge for å ha rett til økonomisk sosialhjelp. Hvis du oppholder deg i utlandet, har du ikke rett til økonomisk sosialhjelp.");
            pdf.addBlankLine();
            pdf.skrivTekstBold(getTekst("informasjon.nodsituasjon.undertittel"));
            pdf.skrivTekst("Hvis du ikke har penger til det aller mest nødvendige, som mat, bør du kontakte NAV-kontoret ditt før du sender inn søknaden eller så snart som mulig etter du har søkt. NAV skal også hjelpe deg med å finne et midlertidig botilbud hvis du ikke har et sted å sove eller oppholde deg det nærmeste døgnet.");
            pdf.addBlankLine();
            pdf.skrivTekstBold(getTekst("informasjon.tekster.personopplysninger.innhenting.tittel"));
            pdf.skrivTekst(getTekst("informasjon.tekster.personopplysninger.innhenting.tekst"));
            pdf.addBlankLine();
            pdf.skrivTekstBold(getTekst("informasjon.tekster.personopplysninger.fordusender.tittel"));
            pdf.skrivTekst(getTekst("informasjon.tekster.personopplysninger.fordusender.tekst"));
            pdf.addBlankLine();
            pdf.skrivTekstBold(getTekst("informasjon.tekster.personopplysninger.ettersendt.tittel"));
            pdf.skrivTekst(getTekst("informasjon.tekster.personopplysninger.ettersendt.tekst"));
            pdf.addBlankLine();
            pdf.skrivTekstBold(getTekst("informasjon.tekster.personopplysninger.rettigheter.tittel"));
            pdf.skrivTekst(getTekst("informasjon.tekster.personopplysninger.rettigheter.tekst"));
            pdf.addBlankLine();
            pdf.skrivTekst(getTekst("informasjon.tekster.personopplysninger.sporsmal"));
            pdf.addBlankLine();

            Map<String, String> urisOnPage = new HashMap<>();
            urisOnPage.put("Les mer om andre muligheter", "https://www.nav.no/sosialhjelp/");
            urisOnPage.put("kontakte NAV-kontoret", "https://www.nav.no/person/personopplysninger/#ditt-nav-kontor");
            addLinks(pdf, urisOnPage);
        }
    }

    private void leggTilJuridiskInformasjon(PdfGenerator pdf, JsonSoknad soknad, boolean utvidetSoknad) throws IOException {
        if (utvidetSoknad && soknad.getMottaker() != null && soknad.getMottaker().getNavEnhetsnavn() != null) {
            String navkontor = soknad.getMottaker().getNavEnhetsnavn();
            pdf.skrivH4Bold("Oppsummering");
            pdf.skrivTekst("Søknaden din blir sendt til " + navkontor + ". Dette kontoret har ansvar for å behandle søknaden din, og " + navkontor + " lagrer opplysningene fra søknaden.");
            pdf.addBlankLine();

            pdf.skrivTekstBold("Informasjon");
            pdf.skrivTekst("Når du søker om økonomisk sosialhjelp digitalt, må du gi opplysninger om deg selv slik at NAV-kontoret ditt kan behandle søknaden. Eksempler på opplysninger er din adresse, familieforhold, inntekter og utgifter.");
            pdf.addBlankLine();
            pdf.skrivTekst("NAV vil også hente opplysninger fra andre registre på vegne av kommunen din som skal behandle søknaden:");
            pdf.addBlankLine();
            pdf.skrivTekst("Personopplysninger fra Folkeregisteret, opplysninger om arbeidsforhold fra Arbeidstakerregisteret, opplysninger om skattbare inntekter fra Skatteetaten, opplysninger om bostøtte fra Husbanken og informasjon om statlige ytelser fra NAV.");
            pdf.addBlankLine();
            pdf.skrivTekst("Du kan være trygg på at personopplysningene dine blir behandlet på en sikker og riktig måte:");
            pdf.skrivTekstMedInnrykk("* Vi skal ikke innhente flere opplysninger enn det som er nødvendig.", INNRYKK_2);
            pdf.skrivTekstMedInnrykk("* NAV har taushetsplikt om alle opplysninger som vi behandler. Hvis offentlige virksomheter eller andre ønsker å få utlevert opplysninger om deg, må de ha hjemmel i lov eller du må gi samtykke til det.", INNRYKK_2);
            pdf.addBlankLine();

            pdf.skrivTekstBold("Behandlingsansvarlig");
            pdf.skrivTekst("Det er " + navkontor + " som er ansvarlig for å behandle søknaden og personopplysningene dine.");
            pdf.addBlankLine();
            pdf.skrivTekst("Henvend deg til kommunen hvis du har spørsmål om personopplysninger. Kommunen har også et personvernombud som du kan kontakte.");
            pdf.addBlankLine();
            pdf.skrivTekst("Arbeids- og velferdsdirektoratet har ansvaret for nav.no og behandler den digitale søknaden som en databehandler på vegne av kommunen.");
            pdf.addBlankLine();

            pdf.skrivTekstBold("Formålet med å samle inn og bruke personopplysninger");
            pdf.skrivTekst("Formålet med søknaden er å samle inn tilstrekkelig opplysninger til at kommunen din kan behandle søknaden om økonomisk sosialhjelp. Opplysninger du gir i den digitale søknaden og opplysninger som blir hentet inn, sendes digitalt fra nav.no til NAV-kontoret ditt. Det blir enklere for deg å søke, og NAV-kontoret ditt mottar søknaden ferdig utfylt med nødvendige vedlegg.");
            pdf.addBlankLine();
            pdf.skrivTekst("Opplysningene i søknaden vil bli brukt til å vurdere om du fyller vilkårene for økonomisk sosialhjelp, og skal ikke lagres lenger enn det som er nødvendig ut fra formålet. Hvis ikke opplysningene skal oppbevares etter arkivloven eller andre lover, skal de slettes etter bruk.");
            pdf.addBlankLine();

            pdf.skrivTekstBold("Lovgrunnlaget");
            pdf.skrivTekst("Lovgrunnlaget for å samle inn informasjon i forbindelse med søknaden din er lov om sosiale tjenester i Arbeids- og velferdsforvaltningen.");
            pdf.addBlankLine();

            pdf.skrivTekstBold("Innhenting av personopplysningene dine");
            pdf.skrivTekst("Du gir selv flere opplysninger når du søker om økonomisk sosialhjelp. I tillegg henter vi opplysninger som NAV har i sine registre, som for eksempel opplysninger om andre ytelser du har fra NAV. Vi henter også opplysninger fra andre offentlige registre som vi har lov til å hente informasjon fra, for eksempel opplysninger om arbeidsforhold fra Arbeidsgiver- og arbeidstakerregisteret.");
            pdf.addBlankLine();

            pdf.skrivTekstBold("Lagring av personopplysningene dine");
            pdf.skrivTekst("Før du sender søknaden lagres opplysningene på nav.no");
            pdf.skrivTekstMedInnrykk("Søknader som er påbegynt, men ikke fullført, blir lagret hos Arbeids- og velferdsdirektoratet i to uker. Deretter slettes de.", INNRYKK_2);
            pdf.addBlankLine();
            pdf.skrivTekst("Etter du har sendt søknaden har kommunen din ansvaret for opplysningene om deg");
            pdf.skrivTekstMedInnrykk("Når du sender søknaden din bruker vi KS (Kommunesektorens organisasjon) sin skytjeneste for digital post (Svarut).  Kommunen henter søknaden din i Svarut og lagrer opplysningene i sitt kommunale fagsystem.  Kommunen din har ansvaret for lagring og sletting av opplysningene dine både i Svarut og i fagsystemet . Arkivloven bestemmer hvor lenge opplysninger skal lagres. Ta kontakt med kommunen din hvis du har spørsmål om lagringstid.", INNRYKK_2);
            pdf.addBlankLine();

            pdf.skrivTekstBold("Rettigheter som registrert");
            pdf.skrivTekst("Alle har rett på informasjon om og innsyn i egne personopplysninger etter personopplysningsloven.");
            pdf.addBlankLine();
            pdf.skrivTekst("Hvis opplysninger om deg er feil, ufullstendige eller unødvendige, kan du kreve at opplysningene blir rettet eller supplert etter personopplysningsloven. Du kan også i særlige tilfeller be om å få dem slettet, hvis ikke kommunen har en lovpålagt plikt til å lagre opplysningene som dokumentasjon. Slike krav skal besvares kostnadsfritt og senest innen 30 dager.");
            pdf.addBlankLine();
            pdf.skrivTekst("Du har også flere personvernrettigheter, blant annet såkalt <strong>rett til begrensning</strong>: Du kan i visse tilfeller ha rett til å få en begrenset behandling av personopplysningene dine. Hvis du har en slik rett, vil opplysningene bli lagret, men ikke brukt.");
            pdf.addBlankLine();
            pdf.skrivTekst("Du har også <strong>rett til å protestere</strong> mot behandling av personopplysninger: Det vil si at du i enkelte tilfeller kan ha rett til å protestere mot kommunens ellers lovlige behandling av personopplysninger. Behandlingen må da stanses, og hvis du får medhold vil opplysningene eventuelt bli slettet.");
            pdf.addBlankLine();
            pdf.skrivTekst("Du finner en samlet oversikt over dine rettigheter i Datatilsynets veileder De registrertes rettigheter etter nytt regelverk. Kommunen din vil også ha informasjon om behandling av personopplysninger på sine nettsider.");
            pdf.addBlankLine();
            pdf.skrivTekst("Alle spørsmål du har om behandling av personopplysningene dine må du rette til " + navkontor + ".");
            pdf.addBlankLine();

            pdf.skrivTekstBold("Klagerett til Datatilsynet");
            pdf.skrivTekst("Du har rett til å klage til Datatilsynet hvis du ikke er fornøyd med hvordan vi behandler personopplysninger om deg, eller hvis du mener behandlingen er i strid med personvernreglene. Informasjon om hvordan du går frem finner du på nettsidene til Datatilsynet.");
            pdf.addBlankLine();
            pdf.skrivTekst("Personvern og sikkerhet på nav.no");
            pdf.addBlankLine();

            Map<String, String> urisOnPage = new HashMap<>();
            urisOnPage.put("De registrertes rettigheter etter nytt regelverk", "https://www.datatilsynet.no/regelverk-og-skjema/veiledere/de-registrertes-rettigheter-etter-nytt-regelverk/");
            urisOnPage.put("Personvern og sikkerhet på nav.no", "https://www.nav.no/no/nav-og-samfunn/om-nav/personvern-i-arbeids-og-velferdsetaten/personvern-og-sikkerhet-pa-nav.no");
            urisOnPage.put("Datatilsynet", "https://www.datatilsynet.no/");

            addLinks(pdf, urisOnPage);

            pdf.skrivTekstBold("Bekreftet av bruker med følgende informasjonstekst");
            pdf.skrivTekst("Jeg er kjent med at hvis opplysningene jeg har gitt ikke er riktige og fullstendige kan jeg miste retten til stønad.");
            pdf.addBlankLine();
            pdf.skrivTekst("Jeg er også klar over at jeg kan få krav om å betale tilbake det jeg har fått feilaktig utbetalt, og at jeg kan bli anmeldt til politiet hvis jeg med vilje oppgir feil opplysninger.");
            pdf.addBlankLine();
        }
    }

    private void addLinks(PdfGenerator pdf, Map<String, String> uris) throws IOException {
        pdf.skrivTekst("Lenker på siden: ");
        for (Map.Entry<String, String> entry : uris.entrySet()) {
            String name = entry.getKey();
            String uri = entry.getValue();
            pdf.skrivTekst(name + ": " + uri);
        }
        pdf.addBlankLine();
    }

    private void leggTilMetainformasjon(PdfGenerator pdf, JsonSoknad soknad) throws IOException {
        pdf.skrivTekst("Søknaden er sendt " + formaterDatoOgTidspunkt(soknad.getInnsendingstidspunkt()));
        pdf.skrivTekst("Versjonsnummer: " + soknad.getVersion());
        if (soknad.getMottaker() != null) {
            pdf.skrivTekst("Sendt til: " + soknad.getMottaker().getNavEnhetsnavn());
        }
    }

    private void skrivTekstMedGuard(PdfGenerator pdf, String tekst, String key) throws IOException {
        if (tekst != null) {
            pdf.skrivTekst(getTekst(key) + ": " + tekst);
        }
    }

    private void skrivTekstMedGuardOgIkkeUtfylt(PdfGenerator pdf, String tekst, String key) throws IOException {
        if (tekst != null) {
            pdf.skrivTekst(getTekst(key) + ": " + tekst);
        } else {
            skrivIkkeUtfyltMedGuard(pdf, key);
        }
    }

    private void skrivTekstMedGuardOgIkkeUtfylt(PdfGenerator pdf, Integer verdi, String key) throws IOException {
        if (verdi != null) {
            pdf.skrivTekst(getTekst(key) + ": " + verdi);
        } else {
            pdf.skrivTekst(getTekst(key) + ": " + getTekst("oppsummering.ikkeutfylt"));
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

    private void skrivInfotekst(PdfGenerator pdf, String key) throws IOException {
        pdf.skrivTekstBold(getTekst("infotekst.oppsummering.tittel"));
        pdf.skrivTekst(getTekst(key));
        pdf.addBlankLine();
    }

    private void skrivHjelpetest(PdfGenerator pdf, String key) throws IOException {
        pdf.skrivTekstBold(getTekst("hjelpetekst.oppsummering.tittel"));
        pdf.skrivTekst(getTekst(key));
        pdf.addBlankLine();
    }

    private void skrivKnappTilgjengelig(PdfGenerator pdf, String key) throws IOException {
        pdf.skrivTekstBold("Knapp tilgjengelig:");
        pdf.skrivTekst(getTekst(key));
        pdf.addBlankLine();
    }

    private void skrivSvaralternativer(PdfGenerator pdf, List<String> keys) throws IOException {
        pdf.skrivTekstBold("Svaralternativer:");
        for (String key: keys) {
            pdf.skrivTekstMedInnrykk(getTekst(key), INNRYKK_2);
        }
    }

    private void skrivIkkeUtfylt(PdfGenerator pdf) throws IOException {
        pdf.skrivTekst(getTekst("oppsummering.ikkeutfylt"));
    }

    private void skrivIkkeUtfyltMedGuard(PdfGenerator pdf, String key) throws IOException {
        pdf.skrivTekst(getTekst(key) + ": " + getTekst("oppsummering.ikkeutfylt"));
    }

    private void skrivUtBarnebidragAlternativer(PdfGenerator pdf, boolean utvidetSoknad) throws IOException{
        if(utvidetSoknad){
            List<String> svaralternativer = new ArrayList<>(4);
            svaralternativer.add("familie.barn.true.barnebidrag.betaler");
            svaralternativer.add("familie.barn.true.barnebidrag.mottar");
            svaralternativer.add("familie.barn.true.barnebidrag.begge");
            svaralternativer.add("familie.barn.true.barnebidrag.ingen");
            skrivSvaralternativer(pdf, svaralternativer);
        }
    }

    private String formaterDato(String dato, String format) {
        if (dato == null) {
            return "";
        }
        Locale locale = new Locale("nb", "NO");
        LocalDate localDate = new LocalDate(dato);

        return localDate.toString(format, locale);
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

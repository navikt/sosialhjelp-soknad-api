package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.*;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.*;

@Component
public class SosialhjelpPdfGenerator {

    @Inject
    public NavMessageSource navMessageSource;

    @Inject
    public TextHelpers textHelpers;

    public static final String IKKE_UTFYLT = "Ikke utfylt";

    public byte[] generate(JsonInternalSoknad jsonInternalSoknad, boolean utvidetSoknad) {
        try {
            PdfGenerator pdf = new PdfGenerator();

            JsonData data = jsonInternalSoknad.getSoknad().getData();
            JsonPersonalia jsonPersonalia = data.getPersonalia(); // personalia er required

            // Add header
            String heading = getTekst("applikasjon.sidetittel");
            JsonPersonIdentifikator jsonPersonIdentifikator = jsonPersonalia.getPersonIdentifikator(); // required
            JsonSokernavn jsonSokernavn = jsonPersonalia.getNavn();// required

            String navn = getJsonSokerNavnTekst(jsonSokernavn);

            String fnr = jsonPersonIdentifikator.getVerdi(); // required

            leggTilHeading(pdf, heading, navn, fnr);

            leggTilPersonalia(pdf, data.getPersonalia(), jsonInternalSoknad.getMidlertidigAdresse(), utvidetSoknad);
            leggTilBegrunnelse(pdf, data.getBegrunnelse());
            leggTilArbeidOgUtdanning(pdf, data.getArbeid(), data.getUtdanning());
            leggTilFamilie(pdf, data.getFamilie());
            leggTilBosituasjon(pdf, data.getBosituasjon());
            leggTilInntektOgFormue(pdf, data.getOkonomi());



            return pdf.finish();

        } catch (IOException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }

    private String getJsonSokerNavnTekst(JsonSokernavn jsonSokernavn) {
        return getJsonNavnTekst(jsonSokernavn);
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
    }

    private void leggTilPersonalia(PdfGenerator pdf, JsonPersonalia jsonPersonalia, JsonAdresse midlertidigAdresse, boolean utvidetSoknad) throws IOException {

        pdf.addBlankLine();
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
                    JsonGateAdresse gaf = (JsonGateAdresse) jsonPersonalia.getFolkeregistrertAdresse();
                    folkeregistrertAdresseTekst = gaf.getGatenavn() + " " + gaf.getHusnummer() + gaf.getHusbokstav() + ", " + gaf.getPostnummer() + " " + gaf.getPoststed();
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
            pdf.skrivTekstMedInnrykk(folkeregistrertAdresseTekst, INNRYKK_2);
            pdf.addBlankLine();
        }

        if (jsonPersonalia.getOppholdsadresse() != null) {
            pdf.skrivTekst(getTekst("soknadsmottaker.infotekst.tekst"));
            String oppholdsAdresseTekst = "";
            switch (jsonPersonalia.getOppholdsadresse().getType()) {
                case GATEADRESSE:
                    JsonGateAdresse gaf = (JsonGateAdresse) jsonPersonalia.getOppholdsadresse();
                    oppholdsAdresseTekst = gaf.getGatenavn() + " " + gaf.getHusnummer() + gaf.getHusbokstav() + ", " + gaf.getPostnummer() + " " + gaf.getPoststed();
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
            pdf.skrivTekstMedInnrykk(oppholdsAdresseTekst, INNRYKK_2);
            pdf.addBlankLine();
        }

        if (utvidetSoknad) {
            pdf.skrivTekst("Valgt adresse:");
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

        // Telefonnummer
        JsonTelefonnummer jsonTelefonnummer = jsonPersonalia.getTelefonnummer();
        if (jsonTelefonnummer != null) {
            if (jsonTelefonnummer.getKilde() == JsonKilde.SYSTEM) {
                pdf.skrivTekstBold(getTekst("kontakt.system.telefoninfo.sporsmal"));
                if (utvidetSoknad) {
                    skrivInfotekst(pdf, "kontakt.system.telefoninfo.infotekst.tekst");
                }
                pdf.skrivTekst(getTekst("kontakt.system.telefon.label"));
                pdf.skrivTekstMedInnrykk(jsonTelefonnummer.getVerdi(), INNRYKK_2);
                pdf.addBlankLine();
                if (utvidetSoknad) {
                    skrivKnappTilgjengelig(pdf, "kontakt.system.telefon.endreknapp.label");
                }
            } else {
                pdf.skrivTekstBold(getTekst("kontakt.telefon.sporsmal"));
                pdf.skrivTekst(getTekst("kontakt.telefon.label"));
                if (jsonTelefonnummer.getVerdi() == null || jsonTelefonnummer.getVerdi().isEmpty()) {
                    pdf.skrivTekst(getTekst("oppsummering.ikkeutfylt"));
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
                pdf.skrivTekstMedInnrykk(getTekst("kontakt.kontonummer.harikke.true"), INNRYKK_2);
            } else {
                if (jsonKontonummer.getVerdi() == null || jsonKontonummer.getVerdi().isEmpty()) {
                    pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
                } else {
                    pdf.skrivTekstMedInnrykk(jsonKontonummer.getVerdi(), INNRYKK_2);
                }
            }
            pdf.addBlankLine();
            if (utvidetSoknad) {
                if (jsonKontonummer.getKilde() == JsonKilde.SYSTEM) {
                    skrivKnappTilgjengelig(pdf, "kontakt.system.kontonummer.endreknapp.label");
                } else {
                    List<String> svaralternativer = new ArrayList<>(2);
                    svaralternativer.add("kontakt.kontonummer.harikke");
                    skrivSvaralternativer(pdf, svaralternativer);
                    skrivKnappTilgjengelig(pdf, "systeminfo.avbrytendringknapp.label");
                    skrivInfotekst(pdf, "kontakt.kontonummer.infotekst.tekst");
                }
            }
        }
    }

    private void leggTilUtvidetInfoAdresse(PdfGenerator pdf, JsonAdresse jsonAdresse) throws IOException {
        JsonGateAdresse jsonGateAdresse = (JsonGateAdresse) jsonAdresse;
        pdf.skrivTekstMedInnrykk(jsonGateAdresse.getGatenavn() + " " + jsonGateAdresse.getHusnummer() + "" + jsonGateAdresse.getHusbokstav(), INNRYKK_4);
        pdf.skrivTekstMedInnrykk(jsonGateAdresse.getPostnummer() + " " + jsonGateAdresse.getPoststed(), INNRYKK_4);
    }

    private void leggTilBegrunnelse(PdfGenerator pdf, JsonBegrunnelse jsonBegrunnelse) throws IOException {
        pdf.addBlankLine();
        pdf.skrivH4Bold(getTekst("begrunnelsebolk.tittel"));
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("begrunnelse.hva.sporsmal"));
        pdf.skrivTekst(jsonBegrunnelse.getHvaSokesOm());
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("begrunnelse.hvorfor.sporsmal"));
        pdf.skrivTekst(jsonBegrunnelse.getHvorforSoke());
        pdf.addBlankLine();
    }

    private void leggTilArbeidOgUtdanning(PdfGenerator pdf, JsonArbeid arbeid, JsonUtdanning utdanning) throws IOException {
        pdf.addBlankLine();
        pdf.skrivH4Bold(getTekst("opplysninger.arbeid.sporsmal"));
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("arbeidsforhold.sporsmal"));
        pdf.addBlankLine();

        if (arbeid != null && arbeid.getForhold() != null && arbeid.getForhold().size() > 0) {

            List<JsonArbeidsforhold> forholdsliste = arbeid.getForhold();
            for (JsonArbeidsforhold forhold : forholdsliste) {
                skrivTekstMedGuard(pdf, forhold.getArbeidsgivernavn(), "arbeidsforhold.arbeidsgivernavn.label");
                if (forhold.getStillingstype() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.stillingstype.label") + ": " + forhold.getStillingstype());
                }
                if (forhold.getStillingsprosent() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.stillingsprosent.label") + ": " + forhold.getStillingsprosent());
                }
                skrivTekstMedGuard(pdf, forhold.getFom(), "arbeidsforhold.fom.label");
                skrivTekstMedGuard(pdf, forhold.getTom(), "arbeidsforhold.tom.label");
                pdf.addBlankLine();
            }

        } else {
            pdf.skrivTekst(getTekst("arbeidsforhold.ingen"));
        }
        if (arbeid != null && arbeid.getKommentarTilArbeidsforhold() != null && arbeid.getKommentarTilArbeidsforhold().getVerdi() != null) {
            pdf.addBlankLine();
            pdf.skrivTekst(getTekst("opplysninger.arbeidsituasjon.kommentarer.label"));
            pdf.addBlankLine();
            pdf.skrivTekstMedInnrykk(arbeid.getKommentarTilArbeidsforhold().getVerdi(), INNRYKK_1);
        }

        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("arbeid.dinsituasjon.studerer.undertittel"));
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("dinsituasjon.studerer.sporsmal"));
        if (utdanning != null && utdanning.getErStudent() != null) {
            if (utdanning.getErStudent()) {
                pdf.skrivTekst(getTekst("dinsituasjon.studerer.true"));
                if (utdanning.getStudentgrad() != null) {
                    pdf.skrivTekst(getTekst("dinsituasjon.studerer.true.grad.sporsmal") + ". Studentgrad: " + utdanning.getStudentgrad());
                }
            } else {
                pdf.skrivTekst(getTekst("dinsituasjon.studerer.false"));
            }
        } else {
            pdf.skrivTekstKursiv(IKKE_UTFYLT);
        }
        pdf.addBlankLine();
    }

    private void leggTilFamilie(PdfGenerator pdf, JsonFamilie familie) throws IOException {

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
                    if (status != null) {
                        if (status.toString().equals("gift")) {
                            pdf.skrivTekst(getTekst("system.familie.sivilstatus.gift")); // Gift/registrert partner

                            leggTilEktefelle(pdf, sivilstatus);
                        } else {
                            pdf.skrivTekst(status.toString());
                        }
                    }
                }

                // Bruker
                if (kilde != null && kilde.equals(JsonKilde.BRUKER)) {
                    JsonSivilstatus.Status status = sivilstatus.getStatus();

                    if (status != null) {

                        if (status.toString().equals("gift")) {
                            pdf.skrivTekst(getTekst("system.familie.sivilstatus.gift")); // Gift/registrert partner


                            leggTilEktefelle(pdf, sivilstatus);


                        } else {
                            pdf.skrivTekst(sivilstatus.getStatus().toString());
                        }

                    } else {
                        pdf.skrivTekstKursiv(IKKE_UTFYLT);
                    }
                }

            } else {
                pdf.skrivTekstKursiv(IKKE_UTFYLT);
            }

            pdf.addBlankLine();


            // FolkeregistrertMedEktefelleAvviksforklaring


            // Forsørgerplikt
            JsonForsorgerplikt forsorgerplikt = familie.getForsorgerplikt();
            if (forsorgerplikt != null) {
                JsonHarForsorgerplikt harForsorgerplikt = forsorgerplikt.getHarForsorgerplikt();
                if (harForsorgerplikt != null && harForsorgerplikt.getVerdi()) {

                    pdf.skrivTekstBold(getTekst("familie.barn.true.barn.sporsmal"));

                    pdf.addBlankLine();

                    List<JsonAnsvar> listeOverAnsvar = forsorgerplikt.getAnsvar();
                    for (JsonAnsvar ansvar : listeOverAnsvar) {
                        JsonBarn barn = ansvar.getBarn();

                        // navn
                        JsonNavn navnPaBarn = barn.getNavn();
                        String navnPaBarnTekst = getJsonNavnTekst(navnPaBarn);
                        pdf.skrivTekst(getTekst("familie.barn.true.barn.navn.label") + ": " + navnPaBarnTekst);

                        // Fødselsdato
                        String fodselsdato = barn.getFodselsdato();
                        skrivTekstMedGuard(pdf, fodselsdato, "kontakt.system.personalia.fnr");

                        // Personnummer
                        String personIdentifikator = barn.getPersonIdentifikator();
                        skrivTekstMedGuard(pdf, personIdentifikator, "kontakt.system.personalia.fnr");

                        // Samme folkeregistrerte adresse
                        JsonErFolkeregistrertSammen erFolkeregistrertSammen = ansvar.getErFolkeregistrertSammen();
                        if (erFolkeregistrertSammen != null) {
                            pdf.skrivTekst(getTekst("familierelasjon.samme_folkeregistrerte_adresse"));
                            Boolean erFolkeregistrertSammenVerdi = erFolkeregistrertSammen.getVerdi();
                            if (erFolkeregistrertSammenVerdi) {
                                pdf.skrivTekstMedInnrykk("Ja", INNRYKK_1);
                                leggTilDeltBosted(pdf, ansvar, true);
                            } else {
                                pdf.skrivTekstMedInnrykk("Nei", INNRYKK_1);
                                leggTilDeltBosted(pdf, ansvar, false);
                            }
                        }


                        pdf.addBlankLine();
                    }

                    pdf.addBlankLine();

                    if (listeOverAnsvar.size() > 0) {

                        // Mottar eller betaler du barnebidrag for ett eller flere av barna?
                        pdf.skrivTekstBold(getTekst("familie.barn.true.barnebidrag.sporsmal"));

                        JsonBarnebidrag barnebidrag = forsorgerplikt.getBarnebidrag();
                        if (barnebidrag != null && barnebidrag.getVerdi() != null) {
                            JsonBarnebidrag.Verdi barnebidragVerdi = barnebidrag.getVerdi();
                            if (barnebidragVerdi != null) {

                                switch (barnebidragVerdi) {
                                    case BETALER:
                                        pdf.skrivTekst(getTekst("familie.barn.true.barnebidrag.betaler"));
                                        break;
                                    case MOTTAR:
                                        pdf.skrivTekst(getTekst("familie.barn.true.barnebidrag.mottar"));

                                        break;
                                    case BEGGE:
                                        pdf.skrivTekst(getTekst("familie.barn.true.barnebidrag.begge"));
                                        break;
                                    case INGEN:
                                        pdf.skrivTekst(getTekst("familie.barn.true.barnebidrag.ingen"));
                                        break;
                                }
                            }
                        } else {
                            pdf.skrivTekstKursiv(IKKE_UTFYLT);
                        }
                    }

                }
            }


        } else {
            pdf.skrivTekstKursiv(IKKE_UTFYLT);
        }

        pdf.addBlankLine();
    }

    private void leggTilDeltBosted(PdfGenerator pdf, JsonAnsvar ansvar, Boolean erFolkeregistrertSammenVerdi) throws IOException {
        // Har barnet delt bosted
        pdf.skrivTekst(getTekst("system.familie.barn.true.barn.deltbosted.sporsmal"));
        JsonHarDeltBosted harDeltBosted = ansvar.getHarDeltBosted();
        if (harDeltBosted != null && harDeltBosted.getVerdi() != null) {
            if (harDeltBosted.getVerdi()) {
                pdf.skrivTekstMedInnrykk("Ja", INNRYKK_1);

                JsonSamvarsgrad samvarsgrad = ansvar.getSamvarsgrad();
                if (samvarsgrad != null && samvarsgrad.getVerdi() != null && !erFolkeregistrertSammenVerdi) {
                    pdf.skrivTekst(getTekst("system.familie.barn.true.barn.grad.sporsmal"));
                    Integer samvarsgradVerdi = samvarsgrad.getVerdi();
                    pdf.skrivTekstMedInnrykk(samvarsgradVerdi + "%", INNRYKK_1);
                }

            } else {
                pdf.skrivTekstMedInnrykk("Nei", INNRYKK_1);
            }
        } else {
            pdf.skrivTekstKursiv(IKKE_UTFYLT);
        }
    }

    private void leggTilBosituasjon(PdfGenerator pdf, JsonBosituasjon bosituasjon) throws IOException {
        pdf.skrivH4Bold(getTekst("bosituasjonbolk.tittel"));
        pdf.addBlankLine();

        if (bosituasjon != null) {
            pdf.skrivTekstBold(getTekst("bosituasjon.sporsmal"));
            JsonBosituasjon.Botype botype = bosituasjon.getBotype();
            if (botype != null) {
                pdf.skrivTekst(getTekst("bosituasjon." + botype.value()));
            } else {
                pdf.skrivTekstKursiv(IKKE_UTFYLT);
            }

            pdf.addBlankLine();

            pdf.skrivTekstBold(getTekst("bosituasjon.antallpersoner.sporsmal"));
            Integer antallPersoner = bosituasjon.getAntallPersoner();
            if (antallPersoner != null) {
                pdf.skrivTekst(antallPersoner.toString());
            } else {
                pdf.skrivTekstKursiv(IKKE_UTFYLT);
            }
        }
        pdf.addBlankLine();
    }

    private void leggTilInntektOgFormue(PdfGenerator pdf, JsonOkonomi okonomi) throws IOException {
        pdf.skrivH4Bold(getTekst("inntektbolk.tittel"));
        pdf.addBlankLine();

        if (okonomi != null) {
            List<JsonOkonomiOpplysningUtbetaling> utbetalinger = okonomi.getOpplysninger().getUtbetaling();

            pdf.skrivTekstBold(getTekst("utbetalinger.inntekt.skattbar.tittel"));
            List<JsonOkonomiOpplysningUtbetaling> skatteetaten = utbetalinger
                    .stream()
                    .filter(utbetaling -> utbetaling.getType().equalsIgnoreCase("skatteetaten"))
                    .collect(Collectors.toList());

            if (skatteetaten.isEmpty()) {
                pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.ingen"));
            } else {
                skatteetaten
                        .forEach(utbetaling -> {
                            try {
                                pdf.skrivTekstBold(getTekst("opplysninger.inntekt.undertittel"));
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.arbeidsgivernavn.label") + ": " + utbetaling.getOrganisasjon().getNavn());
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.periodeFom.label") + ": " + utbetaling.getPeriodeFom());
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.periodeTom.label") + ": " + utbetaling.getPeriodeTom());
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.brutto.label") + ": " + utbetaling.getBrutto());
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.skattetrekk.label") + ": " + utbetaling.getSkattetrekk());
                            } catch (IOException e) {
                                // Handle later
                            }
                        });
            }
            pdf.addBlankLine();

            pdf.skrivTekstBold(getTekst("navytelser.sporsmal"));
            List<JsonOkonomiOpplysningUtbetaling> navytelse = utbetalinger
                    .stream()
                    .filter(utbetaling -> utbetaling.getType().equalsIgnoreCase("navytelse"))
                    .collect(Collectors.toList());
            if (navytelse.isEmpty()) {
                pdf.skrivTekst(getTekst("utbetalinger.ingen.true"));
            } else {
                navytelse
                        .forEach(utbetaling -> {
                            try {
                                pdf.skrivTekstBold("Ytelse");
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.netto.label") + ": " + utbetaling.getNetto());
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.brutto.label") + ": " + utbetaling.getBrutto());
                                pdf.skrivTekst(getTekst("utbetalinger.utbetaling.erutbetalt.label") + ": " + utbetaling.getUtbetalingsdato());
                            } catch (IOException e) {
                                // Handle later
                            }
                        });
            }
            pdf.addBlankLine();

            // Legg til lånekassen dersom student

            pdf.skrivTekstBold(getTekst("inntekt.bostotte.overskrift"));
            List<JsonOkonomiOpplysningUtbetaling> husbanken = utbetalinger
                    .stream()
                    .filter(utbetaling -> utbetaling.getType().equalsIgnoreCase("husbanken"))
                    .collect(Collectors.toList());
            if (husbanken.isEmpty()) {
                pdf.skrivTekst(getTekst("inntekt.bostotte.ikkefunnet"));
            } else {
                husbanken
                        .forEach(utbetaling -> {
                            try {
                                pdf.skrivTekstBold(getTekst("inntekt.bostotte.utbetaling"));
                                pdf.skrivTekst(getTekst("inntekt.bostotte.utbetaling.mottaker") + ": " + utbetaling.getMottaker());
                                pdf.skrivTekst(getTekst("inntekt.bostotte.utbetaling.utbetalingsdato") + ": " + utbetaling.getUtbetalingsdato());
                                pdf.skrivTekst(getTekst("inntekt.bostotte.utbetaling.belop") + ": " + utbetaling.getNetto());
                            } catch (IOException e) {
                                //
                            }
                        });
            }
            pdf.addBlankLine();
        }
    }

    private void skrivTekstMedGuard(PdfGenerator pdf, String tekst, String key) throws IOException {
        if (tekst != null) {
            pdf.skrivTekst(getTekst(key) + ": " + tekst);
        }
    }

    private void leggTilEktefelle(PdfGenerator pdf, JsonSivilstatus sivilstatus) throws IOException {
        JsonEktefelle ektefelle = sivilstatus.getEktefelle();
        Boolean ektefelleHarDiskresjonskode = sivilstatus.getEktefelleHarDiskresjonskode();

        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("familie.sivilstatus.gift.ektefelle.sporsmal"));
        if (!(ektefelleHarDiskresjonskode != null && ektefelleHarDiskresjonskode)) {


            if (ektefelle != null) {

                // Navn
                JsonNavn navn = ektefelle.getNavn();
                String fullstendigNavn = getJsonNavnTekst(navn);
                pdf.skrivTekst(
                        getTekst("familie.sivilstatus.gift.navn.label") + ": " + fullstendigNavn
                );

                // Fødselsnummer
                String personIdentifikator = ektefelle.getPersonIdentifikator();
                skrivTekstMedGuard(pdf, personIdentifikator, "kontakt.system.personalia.fnr");

                // Bor sammen?
                pdf.skrivTekst(getTekst("familie.sivilstatus.gift.ektefelle.borsammen.sporsmal"));
                Boolean borSammenMed = sivilstatus.getBorSammenMed();
                if (borSammenMed != null) {
                    if (borSammenMed) {
                        pdf.skrivTekstMedInnrykk(getTekst("familie.sivilstatus.gift.ektefelle.borsammen.true"), INNRYKK_1);
                    } else {
                        pdf.skrivTekstMedInnrykk(getTekst("familie.sivilstatus.gift.ektefelle.borsammen.false"), INNRYKK_1);
                    }
                } else {
                    pdf.skrivTekstKursiv(IKKE_UTFYLT);
                }

            } else {
                pdf.skrivTekstKursiv(IKKE_UTFYLT);
            }
        } else {
            pdf.skrivTekst(getTekst("system.familie.sivilstatus.diskresjonskode"));
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
}

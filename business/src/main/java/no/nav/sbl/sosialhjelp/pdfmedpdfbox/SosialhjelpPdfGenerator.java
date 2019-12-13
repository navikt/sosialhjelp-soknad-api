package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.INNRYKK_1;
import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.INNRYKK_2;

@Component
public class SosialhjelpPdfGenerator {

    @Inject
    public NavMessageSource navMessageSource;
    public static final String IKKE_UTFYLT = "Ikke utfylt";

    public byte[] generate(JsonInternalSoknad jsonInternalSoknad) {
        try {


            PdfGenerator pdf = new PdfGenerator();

            JsonData data = jsonInternalSoknad.getSoknad().getData();
            JsonPersonalia jsonPersonalia = data.getPersonalia(); // personalia er required

            // Add header
            String heading = getTekst("applikasjon.sidetittel");
            JsonPersonIdentifikator jsonPersonIdentifikator = jsonPersonalia.getPersonIdentifikator(); // required
            JsonSokernavn jsonSokernavn = jsonPersonalia.getNavn();// required

            String navn = "";
            if (jsonSokernavn != null) {
                if (jsonSokernavn.getFornavn() != null) {
                    navn += jsonSokernavn.getFornavn();
                }
                if (jsonSokernavn.getMellomnavn() != null) {
                    navn += " " + jsonSokernavn.getMellomnavn();
                }
                if (jsonSokernavn.getEtternavn() != null) {
                    navn += " " + jsonSokernavn.getEtternavn();
                }
            }

            String fnr = jsonPersonIdentifikator.getVerdi(); // required

            leggTilHeading(pdf, heading, navn, fnr);

            leggTilPersonalia(pdf, jsonPersonalia);
            leggTilBegrunnelse(pdf, data.getBegrunnelse());
            leggTilArbeidOgUtdanning(pdf, data.getArbeid(), data.getUtdanning());
            leggTilFamilie(pdf, data.getFamilie());


            return pdf.finish();

        } catch (IOException e) {
            throw new RuntimeException("Error while creating pdf", e);
        }
    }

    public void setNavMessageSource(NavMessageSource navMessageSource) {
        this.navMessageSource = navMessageSource;
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

    private void leggTilPersonalia(PdfGenerator pdf, JsonPersonalia jsonPersonalia) throws IOException {

        pdf.addBlankLine();
        pdf.skrivH4Bold(getTekst("kontakt.tittel"));
        pdf.addBlankLine();

        // Statsborgerskap
        JsonStatsborgerskap jsonStatsborgerskap = jsonPersonalia.getStatsborgerskap();
        pdf.skrivTekstBold(getTekst("kontakt.system.personalia.statsborgerskap"));
        if (jsonStatsborgerskap != null && jsonStatsborgerskap.getVerdi() != null) {
            String statsborgerskap = jsonPersonalia.getStatsborgerskap().getVerdi();
            pdf.skrivTekst(statsborgerskap);
        }
        pdf.addBlankLine();

        // Adresse
        pdf.skrivTekstBold(getTekst("kontakt.system.adresse"));
        pdf.addBlankLine();

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
        }

        pdf.addBlankLine();

        // Telefonnummer
        JsonTelefonnummer jsonTelefonnummer = jsonPersonalia.getTelefonnummer();
        if (jsonTelefonnummer != null) {
            pdf.skrivTekstBold(getTekst("kontakt.system.telefon.label"));
            pdf.skrivTekst(getTekst("kontakt.telefon.label"));
            pdf.skrivTekstMedInnrykk(jsonTelefonnummer.getVerdi(), INNRYKK_2);
        }

        pdf.addBlankLine();

        // Kontonummer
        JsonKontonummer jsonKontonummer = jsonPersonalia.getKontonummer();
        if (jsonKontonummer != null) {
            pdf.skrivTekstBold(getTekst("kontakt.kontonummer.harikke.sporsmal"));
            pdf.skrivTekst(getTekst("kontakt.kontonummer.label"));
            pdf.skrivTekstMedInnrykk(jsonKontonummer.getVerdi(), INNRYKK_2);
        }
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
                if (forhold.getArbeidsgivernavn() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.arbeidsgivernavn.label") + ": " + forhold.getArbeidsgivernavn());
                }
                if (forhold.getStillingstype() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.stillingstype.label") + ": " + forhold.getStillingstype());
                }
                if (forhold.getStillingsprosent() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.stillingsprosent.label") + ": " + forhold.getStillingsprosent());
                }
                if (forhold.getFom() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.fom.label") + ": " + forhold.getFom());
                }
                if (forhold.getTom() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.tom.label") + ": " + forhold.getTom());
                }
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

                if (kilde != null && kilde.equals(JsonKilde.SYSTEM)) {
                    pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.sporsmal"));
                    pdf.skrivTekst(sivilstatus.getStatus().toString());

                    if (sivilstatus.getEktefelleHarDiskresjonskode() != null && sivilstatus.getEktefelleHarDiskresjonskode()) {
                        pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.ikkeTilgang.label"));
                        pdf.skrivTekst(getTekst("system.familie.sivilstatus.diskresjonskode"));
                    } else {
                        pdf.skrivTekst("Burde se dette");
                        pdf.skrivTekst(getTekst("system.familie.sivilstatus.gift.ektefelle.navn"));
                    }

                }

                if (kilde != null && kilde.equals(JsonKilde.BRUKER)) {
                    JsonSivilstatus.Status status = sivilstatus.getStatus();

                    if (status != null) {

                        if (status.toString().equals("gift")) {
                            pdf.skrivTekst(getTekst("system.familie.sivilstatus.gift")); // Gift/registrert partner

                            pdf.addBlankLine();

                            pdf.skrivTekstBold(getTekst("familie.sivilstatus.gift.ektefelle.sporsmal"));
                            JsonEktefelle ektefelle = sivilstatus.getEktefelle();
                            if (ektefelle != null) {

                                // Navn
                                JsonNavn navn = ektefelle.getNavn();
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
                                pdf.skrivTekst(
                                        getTekst("familie.sivilstatus.gift.navn.label") + ": " + fullstendigNavn
                                );

                                // Fødselsnummer
                                String personIdentifikator = ektefelle.getPersonIdentifikator();
                                if (personIdentifikator != null) {
                                    pdf.skrivTekst(getTekst("kontakt.system.personalia.fnr") + ": " + personIdentifikator);
                                }


                            } else {
                                pdf.skrivTekstKursiv(IKKE_UTFYLT);
                            }

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
                            pdf.skrivTekst(sivilstatus.getStatus().toString());
                        }

                    } else {
                        pdf.skrivTekstKursiv(IKKE_UTFYLT);
                    }
                }

            } else {
                pdf.skrivTekstKursiv(IKKE_UTFYLT);
            }

        } else {
            pdf.skrivTekstKursiv(IKKE_UTFYLT);
        }


        // FolkeregistrertMedEktefelleAvviksforklaring

        // Forsørgerplikt
    }
}

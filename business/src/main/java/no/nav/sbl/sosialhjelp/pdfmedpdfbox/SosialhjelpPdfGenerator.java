package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
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
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static no.nav.sbl.sosialhjelp.pdfmedpdfbox.PdfGenerator.*;
import static org.apache.cxf.common.logging.LogUtils.getLogger;

@Component
public class SosialhjelpPdfGenerator {

    private final Logger logger = getLogger(SosialhjelpPdfGenerator.class);

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
                if (forhold.getArbeidsgivernavn() != null) {
                    pdf.skrivTekst(forhold.getArbeidsgivernavn());
                }
                skrivTekstMedGuard(pdf, forhold.getFom(), "arbeidsforhold.fom.label");
                skrivTekstMedGuard(pdf, forhold.getTom(), "arbeidsforhold.tom.label");

                if (forhold.getStillingsprosent() != null) {
                    pdf.skrivTekst(getTekst("arbeidsforhold.stillingsprosent.label") + ": " + forhold.getStillingsprosent());
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
            pdf.skrivTekstMedInnrykk(arbeid.getKommentarTilArbeidsforhold().getVerdi(), INNRYKK_1);
            pdf.addBlankLine();
        } else if (utvidetSoknad) {
            pdf.skrivTekst(getTekst("opplysninger.arbeidsituasjon.kommentarer.label"));
            pdf.addBlankLine();
            pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
            pdf.addBlankLine();
        }

        pdf.skrivTekstBold(getTekst("arbeid.dinsituasjon.studerer.undertittel"));
        pdf.addBlankLine();
        pdf.skrivTekstBold(getTekst("dinsituasjon.studerer.sporsmal"));
        if (utdanning != null && utdanning.getErStudent() != null) {
            pdf.skrivTekst(getTekst("dinsituasjon.studerer." + utdanning.getErStudent()));

        } else {
            pdf.skrivTekstKursiv(IKKE_UTFYLT);
        }

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
                pdf.skrivTekstKursiv(IKKE_UTFYLT);
            }
            pdf.addBlankLine();

            if (utvidetSoknad) {
                List<String> svaralternativer = new ArrayList<>(2);
                svaralternativer.add("dinsituasjon.jobb.true.grad.deltid");
                svaralternativer.add("dinsituasjon.jobb.true.grad.heltid");
                skrivSvaralternativer(pdf, svaralternativer);
            }
        }
        pdf.addBlankLine();
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

                        if (sivilstatus.getEktefelleHarDiskresjonskode() != null && sivilstatus.getEktefelleHarDiskresjonskode()) {
                            pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.ikkeTilgang.label"));
                            pdf.skrivTekst("Ektefelle/partner har diskresjonskode");
                        } else {
                            JsonEktefelle ektefelle = sivilstatus.getEktefelle();
                            if (ektefelle != null) {
                                if (!utvidetSoknad) {
                                    pdf.skrivTekstBold(getTekst("system.familie.sivilstatus.infotekst"));
                                }
                                pdf.skrivTekst(getTekst("system.familie.sivilstatus.gift.ektefelle.navn"));
                                String ektefelleNavn = ektefelle.getNavn().getFornavn();
                                if (ektefelle.getNavn().getMellomnavn() != null) {
                                    ektefelleNavn += " " + ektefelle.getNavn().getMellomnavn();
                                }
                                ektefelleNavn += " " + ektefelle.getNavn().getEtternavn();
                                pdf.skrivTekstMedInnrykk(ektefelleNavn, INNRYKK_2);

                                pdf.addBlankLine();

                                pdf.skrivTekst(getTekst("system.familie.sivilstatus.gift.ektefelle.fodselsdato"));
                                pdf.skrivTekstMedInnrykk(ektefelle.getFodselsdato(), INNRYKK_2);

                                pdf.addBlankLine();

                                pdf.skrivTekst(getTekst("system.familie.sivilstatus.gift.ektefelle.folkereg"));
                                if (sivilstatus.getFolkeregistrertMedEktefelle() != null) {
                                    pdf.skrivTekstMedInnrykk(
                                            getTekst("system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen." +
                                                    sivilstatus.getFolkeregistrertMedEktefelle()), INNRYKK_2);
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
                        if (ektefelle != null) {
                            pdf.skrivTekstBold(getTekst("familie.sivilstatus.gift.ektefelle.sporsmal"));
                            pdf.skrivTekst(getTekst("familie.sivilstatus.gift.ektefelle.fornavn.label"));
                            if (ektefelle.getNavn().getFornavn() != null) {
                                pdf.skrivTekstMedInnrykk(ektefelle.getNavn().getFornavn(), INNRYKK_2);
                            } else {
                                pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
                            }
                            pdf.skrivTekst(getTekst("familie.sivilstatus.gift.ektefelle.mellomnavn.label"));
                            if (ektefelle.getNavn().getMellomnavn() != null) {
                                pdf.skrivTekstMedInnrykk(ektefelle.getNavn().getMellomnavn(), INNRYKK_2);
                            } else {
                                pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
                            }
                            pdf.skrivTekst(getTekst("familie.sivilstatus.gift.ektefelle.etternavn.label"));
                            if (ektefelle.getNavn().getEtternavn() != null) {
                                pdf.skrivTekstMedInnrykk(ektefelle.getNavn().getEtternavn(), INNRYKK_2);
                            } else {
                                pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
                            }

                            pdf.skrivTekst(getTekst("familie.sivilstatus.gift.ektefelle.fnr.label"));
                            if (ektefelle.getFodselsdato() != null) {
                                pdf.skrivTekstMedInnrykk(ektefelle.getFodselsdato(), INNRYKK_2);
                            } else {
                                pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
                            }

                            pdf.skrivTekst(getTekst("familie.sivilstatus.gift.ektefelle.pnr.label"));
                            if (ektefelle.getPersonIdentifikator() != null) {
                                pdf.skrivTekstMedInnrykk(ektefelle.getPersonIdentifikator(), INNRYKK_2);
                            } else {
                                pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
                            }

                            if (ektefelle.getPersonIdentifikator() != null) {
                                pdf.skrivTekst(getTekst("personalia.fnr"));
                                pdf.skrivTekstMedInnrykk(ektefelle.getPersonIdentifikator(), INNRYKK_2);
                            }

                        }

                        if (status == JsonSivilstatus.Status.GIFT) {
                            pdf.skrivTekst(getTekst("familie.sivilstatus.gift.ektefelle.borsammen.sporsmal"));

                            if (sivilstatus.getBorSammenMed() != null) {
                                pdf.skrivTekstMedInnrykk(getTekst("familie.sivilstatus.gift.ektefelle.borsammen." + sivilstatus.getBorSammenMed()), INNRYKK_2);
                            } else {
                                pdf.skrivTekstMedInnrykk(getTekst("oppsummering.ikkeutfylt"), INNRYKK_2);
                            }

                            if (utvidetSoknad) {
                                List<String> borSammenSvaralternativer = new ArrayList<>(2);
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.true");
                                borSammenSvaralternativer.add("familie.sivilstatus.gift.borsammen.false");
                                skrivSvaralternativer(pdf, borSammenSvaralternativer);
                            }
                        }
                    } else {
                        pdf.skrivTekstKursiv(IKKE_UTFYLT);
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
                pdf.skrivTekstKursiv(IKKE_UTFYLT);
            }

            pdf.addBlankLine();

            if(utvidetSoknad){
                pdf.skrivTekstBold(getTekst("familierelasjon.faktum.sporsmal"));
            }
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
                            pdf.addBlankLine();
                            // navn
                            JsonNavn navnPaBarn = barn.getNavn();
                            String navnPaBarnTekst = getJsonNavnTekst(navnPaBarn);
                            pdf.skrivTekst(getTekst("familie.barn.true.barn.navn.label") + ": " + navnPaBarnTekst);

                            // Fødselsdato
                            String fodselsdato = barn.getFodselsdato();
                            skrivTekstMedGuard(pdf, fodselsdato, "kontakt.system.personalia.fnr");

                            // Personnummer TODO: Finnes ikke i søknad eller handlebarkode?
                            //String personIdentifikator = barn.getPersonIdentifikator();
                            // skrivTekstMedGuard(pdf, personIdentifikator, "kontakt.system.personalia.fnr");

                            // Samme folkeregistrerte adresse
                            JsonErFolkeregistrertSammen erFolkeregistrertSammen = ansvar.getErFolkeregistrertSammen();
                            if (erFolkeregistrertSammen != null) {
                                pdf.skrivTekst(getTekst("familierelasjon.samme_folkeregistrerte_adresse"));
                                if (erFolkeregistrertSammen.getVerdi() != null && erFolkeregistrertSammen.getVerdi()) {
                                    pdf.skrivTekstMedInnrykk("Ja", INNRYKK_1);
                                    pdf.addBlankLine();
                                    leggTilDeltBosted(pdf, ansvar, true, utvidetSoknad);
                                } else {
                                    pdf.skrivTekstMedInnrykk("Nei", INNRYKK_1);
                                    pdf.addBlankLine();
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
                        pdf.skrivTekstKursiv(IKKE_UTFYLT);

                    }
                    skrivUtBarnebidragAlternativer(pdf, utvidetSoknad);
                }
            }
            else{
                if(utvidetSoknad){
                    pdf.skrivH3(getTekst("familierelasjon.ingen_registerte_barn_tittel"));
                    pdf.skrivTekst(getTekst("familierelasjon.ingen_registrerte_barn_tekst"));
                }
            }
        } else {
            pdf.skrivTekstKursiv(IKKE_UTFYLT);
        }
        pdf.addBlankLine();
    }

    private void leggTilDeltBosted(PdfGenerator pdf, JsonAnsvar ansvar, Boolean erFolkeregistrertSammenVerdi, boolean utvidetSoknad) throws IOException {
        // Har barnet delt bosted
        if (erFolkeregistrertSammenVerdi) {
            pdf.skrivTekstBold(getTekst("system.familie.barn.true.barn.deltbosted.sporsmal"));

            JsonHarDeltBosted harDeltBosted = ansvar.getHarDeltBosted();
            if (harDeltBosted != null && harDeltBosted.getVerdi() != null) {
                pdf.skrivTekstMedInnrykk(getTekst("system.familie.barn.true.barn.deltbosted." + harDeltBosted.getVerdi()), INNRYKK_1);

                if (utvidetSoknad) {
                    List<String> deltBostedSvaralternativer = new ArrayList<>(2);
                    deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.true");
                    deltBostedSvaralternativer.add("system.familie.barn.true.barn.deltbosted.false");
                    skrivSvaralternativer(pdf, deltBostedSvaralternativer);
                }
            /*


                skrivUtDeltBostedBarnAlternativer(pdf, utvidetSoknad);
                pdf.addBlankLine();

                JsonSamvarsgrad samvarsgrad = ansvar.getSamvarsgrad();
                if (samvarsgrad != null && samvarsgrad.getVerdi() != null && !erFolkeregistrertSammenVerdi) {
                    pdf.skrivTekst(getTekst("system.familie.barn.true.barn.grad.sporsmal"));
                    Integer samvarsgradVerdi = samvarsgrad.getVerdi();
                    pdf.skrivTekstMedInnrykk(samvarsgradVerdi + "%", INNRYKK_1);

                    if(utvidetSoknad){
                        List<String> svaralternativer = new ArrayList<>(1);
                        svaralternativer.add("system.familie.barn.true.barn.grad.pattern");
                        skrivSvaralternativer(pdf, svaralternativer);
                    }
                }
            } else {
                skrivUtDeltBostedBarnAlternativer(pdf, utvidetSoknad);
            }*/
            } else {
                skrivIkkeUtfylt(pdf);
            }

            if(utvidetSoknad){
                skrivHjelpetest(pdf, "system.familie.barn.true.barn.deltbosted.hjelpetekst.tekst");
            }
        } else {
            pdf.skrivTekst(getTekst("system.familie.barn.true.barn.grad.sporsmal"));

            if (ansvar.getSamvarsgrad() != null && ansvar.getSamvarsgrad().getVerdi() != null ) {
                pdf.skrivTekst(ansvar.getSamvarsgrad().getVerdi() + "%");
            } else {
                skrivIkkeUtfylt(pdf);
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
                        pdf.skrivTekst("Inntekt");
                        if (skatt.getOrganisasjon() != null && skatt.getOrganisasjon().getNavn() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.arbeidsgivernavn.label") + ": " + skatt.getOrganisasjon().getNavn());
                        }
                        if (skatt.getPeriodeFom() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.periodeFom.label") + ": " + skatt.getPeriodeFom());
                        }
                        if (skatt.getPeriodeTom() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.periodeTom.label") + ": " + skatt.getPeriodeTom());
                        }
                        if (skatt.getBrutto() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.brutto.label") + ": " + skatt.getBrutto());
                        }
                        if (skatt.getSkattetrekk() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.skattetrekk.label") + ": " + skatt.getSkattetrekk());
                        }
                    }
                    if (utvidetSoknad) {
                        skrivInfotekst(pdf, "utbetalinger.infotekst.tekst");
                    }
                } else {
                    pdf.skrivTekst(getTekst("utbetalinger.inntekt.skattbar.ingen"));
                }
                pdf.addBlankLine();
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
                        pdf.skrivTekst("Ytelse");
                        pdf.skrivTekst(getTekst("utbetalinger.utbetaling.type.label") + ": " + navytelse.getTittel());
                        if (navytelse.getNetto() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.netto.label") + ": " + navytelse.getNetto());
                        }
                        if (navytelse.getBrutto() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.brutto.label") + ": " + navytelse.getBrutto());
                        }
                        if (navytelse.getUtbetalingsdato() != null) {
                            pdf.skrivTekst(getTekst("utbetalinger.utbetaling.erutbetalt.label") + ": " + navytelse.getUtbetalingsdato());
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
                    pdf.skrivTekst(getTekst("inntekt.bostotte.utbetaling"));
                    if (husbanken.getMottaker() != null) {
                        pdf.skrivTekst(getTekst("inntekt.bostotte.utbetaling.mottaker") + ": " + husbanken.getMottaker().value());
                    }
                    pdf.skrivTekst(getTekst("inntekt.bostotte.utbetaling.utbetalingsdato") + ": " + husbanken.getUtbetalingsdato());
                    pdf.skrivTekst(getTekst("inntekt.bostotte.utbetaling.belop") + ": " + husbanken.getNetto());
                }
                JsonBostotte bostotte = okonomi.getOpplysninger().getBostotte();
                if (bostotte != null && bostotte.getSaker() != null) {
                    for (JsonBostotteSak bostotteSak : bostotte.getSaker()) {
                        pdf.skrivTekst("inntekt.bostotte.sak");
                        pdf.skrivTekst(bostotteSak.getDato());
                        pdf.skrivTekst(getTekst("inntekt.bostotte.sak.status"));
                        pdf.skrivTekst(finnSaksStatus(bostotteSak));
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
                }
                pdf.addBlankLine();
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
                    for (JsonOkonomioversiktFormue formue : okonomi.getOversikt().getFormue()) {
                        if (verdierAlternativer.contains(formue.getType())) {
                            pdf.skrivTekst(formue.getTittel());
                            if (formue.getType().equals("annet")) {
                                pdf.skrivTekst(getTekst("inntekt.eierandeler.true.type.annet.true.beskrivelse.label"));
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
                sparingAlternativer.add("annet");

                for (JsonOkonomioversiktFormue formue : okonomi.getOversikt().getFormue()) {
                    if (sparingAlternativer.contains(formue.getType())) {
                        pdf.skrivTekst(formue.getTittel());
                        if (formue.equals("annet")) {
                            pdf.skrivTekst(getTekst("inntekt.bankinnskudd.true.type.annet.true.beskrivelse.label"));
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
                utbetalingAlternativer.add("annet");

                if (utbetalingBekreftelse.getVerdi()) {
                    pdf.skrivTekst(getTekst("inntekt.inntekter.true.type.sporsmal"));
                    for (JsonOkonomiOpplysningUtbetaling utbetaling : okonomi.getOpplysninger().getUtbetaling()) {
                        if (utbetalingAlternativer.contains(utbetaling.getType())) {
                            pdf.skrivTekst(utbetaling.getTittel());
                            if (utbetaling.getType().equals("annet")) {
                                pdf.skrivTekst(getTekst("inntekt.eierandeler.true.type.annet.true.beskrivelse.label"));
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
                    pdf.skrivTekst(getTekst("utgifter.boutgift.true.type.sporsmal"));

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
            if (soknad.getData().getFamilie() != null && soknad.getData().getFamilie().getForsorgerplikt() != null && soknad.getData().getFamilie().getForsorgerplikt().getHarForsorgerplikt().getVerdi()) {
                pdf.skrivTekstBold(getTekst("utgifter.barn.sporsmal"));
                if (utvidetSoknad) {
                    skrivInfotekst(pdf, "utgifter.barn.infotekst.tekst");
                }
                List<JsonOkonomibekreftelse> barneutgifterBekreftelser = hentBekreftelser(okonomi, "barneutgifter");
                if (!barneutgifterBekreftelser.isEmpty()) {
                    JsonOkonomibekreftelse barneutgiftBekreftelse = barneutgifterBekreftelser.get(0);

                    pdf.skrivTekst(getTekst("utgifter.barn." + barneutgiftBekreftelse.getVerdi()));

                    if (barneutgiftBekreftelse.getVerdi()) {
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

        pdf.skrivTekstBold(getTekst("opplysningerbolk.tittel"));
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
                pdf.skrivTekst(getTekst("opplysninger.inntekt.bostotte.utbetaling.label"));
            }
            if (inntekt.getType().equals("studielanOgStipend")) {
                pdf.skrivTekst(getTekst("opplysninger.arbeid.student.utbetaling.label"));
            }
            if (inntekt.getType().equals("jobb")) {
                pdf.skrivTekst(getTekst("opplysninger.arbeid.jobb.bruttolonn.label"));
                if (inntekt.getBrutto() != null) {
                    pdf.skrivTekst(inntekt.getBrutto().toString());
                } else {
                    skrivIkkeUtfylt(pdf);
                }
                pdf.skrivTekst(getTekst("opplysninger.arbeid.jobb.nettolonn.label"));
            }
            if (inntekt.getType().equals("barnebidrag")) {
                pdf.skrivTekst(getTekst("opplysninger.familiesituasjon.barnebidrag.mottar.mottar.label"));
            }
            if (inntekt.getNetto() != null) {
                pdf.skrivTekst(inntekt.getNetto().toString());
            } else {
                skrivIkkeUtfylt(pdf);
            }
        }

        // Formue
        List<String> sparingTyper = new ArrayList<>(3);
        sparingTyper.add("brukskonto");
        sparingTyper.add("bsu");
        sparingTyper.add("sparekonto");
        for (JsonOkonomioversiktFormue formue : okonomi.getOversikt().getFormue()) {
            if (sparingTyper.contains(formue.getType())) {
                pdf.skrivTekst(formue.getTittel());
                pdf.skrivTekst(getTekst("opplysninger.inntekt.bankinnskudd." + formue.getType() + ".saldo.label"));
                if (formue.getBelop() != null) {
                    pdf.skrivTekst(formue.getBelop().toString());
                } else {
                    skrivIkkeUtfylt(pdf);
                }
            }
        }

        // Utbetaling
        for (JsonOkonomiOpplysningUtbetaling utbetaling : okonomi.getOpplysninger().getUtbetaling()) {
            if (!utbetaling.getType().equals("skatteetaten") && !utbetaling.getType().equals("navytelse")) {
                pdf.skrivTekst(utbetaling.getTittel());
                if (utbetaling.getType().equals("sluttoppgjoer")) {
                    pdf.skrivTekst(getTekst("opplysninger.arbeid.avsluttet.netto.label"));
                } else {
                    pdf.skrivTekst(getTekst("opplysninger.inntekt.inntekter." + utbetaling.getType() + ".sum.label"));
                }
                if (utbetaling.getBelop() != null) {
                    pdf.skrivTekst(utbetaling.getBelop().toString());
                } else {
                    skrivIkkeUtfylt(pdf);
                }
            }
        }

        // Utgift
        pdf.skrivTekstBold(getTekst("utgifterbolk.tittel"));
        for (JsonOkonomiOpplysningUtgift utgift : okonomi.getOpplysninger().getUtgift()) {
            pdf.skrivTekst(utgift.getTittel());

            if (utgifterBarnAlternativer.contains(utgift.getType())) {
                pdf.skrivTekst(getTekst("opplysninger.utgifter.barn." + utgift.getType() + ".sisteregning.label"));
            }
            if (boutgiftAlternativer.contains(utgift.getType())) {
                pdf.skrivTekst(getTekst("opplysninger.utgifter.boutgift." + utgift.getType() + ".sisteregning.label"));
            }

            if (utgift.getType().equals("annen")) {
                pdf.skrivTekst(getTekst("opplysninger.ekstrainfo.utgifter.utgift.label"));
            }
            if (utgift.getBelop() != null) {
                pdf.skrivTekst(utgift.getBelop().toString());
            } else {
                skrivIkkeUtfylt(pdf);
            }
        }
        for (JsonOkonomioversiktUtgift utgift : okonomi.getOversikt().getUtgift()) {
            pdf.skrivTekst(utgift.getTittel());

            if (utgifterBarnAlternativer.contains(utgift.getType())) {
                pdf.skrivTekst(getTekst( "opplysninger.utgifter.barn." + utgift.getType() + ".sistemnd.label"));
            }
            if (utgift.getType().equals("barnebidrag")) {
                pdf.skrivTekst(getTekst("opplysninger.familiesituasjon.barnebidrag.betaler.betaler.label"));
            }
            if (utgift.getType().equals("husleie")) {
                pdf.skrivTekst(getTekst("opplysninger.utgifter.boutgift.husleie.permnd.label"));
            }
            if (utgift.getType().equals("boliglanAvdrag")) {
                pdf.skrivTekst(getTekst("opplysninger.utgifter.boutgift.avdraglaan.avdrag.label"));
            }
            if (utgift.getType().equals("boliglanRenter")) {
                pdf.skrivTekst(getTekst("opplysninger.utgifter.boutgift.avdraglaan.renter.label"));
            }
            if (utgift.getBelop() != null) {
                pdf.skrivTekst(utgift.getBelop().toString());
            } else {
                skrivIkkeUtfylt(pdf);
            }
        }

        // Vedlegg
        pdf.skrivTekstBold(getTekst("vedlegg.oppsummering.tittel"));
        if (vedleggSpesifikasjon != null && vedleggSpesifikasjon.getVedlegg() != null) {
            for (JsonVedlegg vedlegg : vedleggSpesifikasjon.getVedlegg()) {
                pdf.skrivTekst(getTekst("vedlegg." + vedlegg.getType() + "." + vedlegg.getTilleggsinfo() + ".tittel"));
                if (vedlegg.getFiler() != null) {
                    for (JsonFiler fil : vedlegg.getFiler()) {
                        pdf.skrivTekst(fil.getFilnavn());
                    }
                }
                if (vedlegg.getStatus() != null && vedlegg.getStatus().equals("VedleggKreves")) {
                    pdf.skrivTekst(getTekst("vedlegg.oppsummering.ikkelastetopp"));
                }
                if (vedlegg.getStatus() != null && vedlegg.getStatus().equals("VedleggAlleredeSendt")) {
                    pdf.skrivTekst(getTekst("opplysninger.vedlegg.alleredelastetopp"));
                }
            }
        }
    }

    private void skrivTekstMedGuard(PdfGenerator pdf, String tekst, String key) throws IOException {
        if (tekst != null) {
            pdf.skrivTekst(getTekst(key) + ": " + tekst);
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
}

package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.soknad.pdf.PdfUtils;
import no.nav.sosialhjelp.soknad.pdf.TextHelpers;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.apache.commons.lang3.LocaleUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static no.nav.sosialhjelp.soknad.pdf.ArbeidOgUtdanningKt.leggTilArbeidOgUtdanning;
import static no.nav.sosialhjelp.soknad.pdf.BegrunnelseKt.leggTilBegrunnelse;
import static no.nav.sosialhjelp.soknad.pdf.BosituasjonKt.leggTilBosituasjon;
import static no.nav.sosialhjelp.soknad.pdf.FamilieKt.leggTilFamilie;
import static no.nav.sosialhjelp.soknad.pdf.InformasjonFraForsideKt.leggTilInformasjonFraForsiden;
import static no.nav.sosialhjelp.soknad.pdf.InntektOgFormueKt.leggTilInntektOgFormue;
import static no.nav.sosialhjelp.soknad.pdf.JuridiskInformasjonKt.leggTilJuridiskInformasjon;
import static no.nav.sosialhjelp.soknad.pdf.MetainformasjonKt.leggTilMetainformasjon;
import static no.nav.sosialhjelp.soknad.pdf.OkonomiskeOpplysningerOgVedleggKt.leggTilOkonomiskeOpplysningerOgVedlegg;
import static no.nav.sosialhjelp.soknad.pdf.PersonaliaKt.leggTilPersonalia;
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

            leggTilPersonalia(pdf, pdfUtils, textHelpers, data.getPersonalia(), jsonInternalSoknad.getMidlertidigAdresse(), utvidetSoknad);
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

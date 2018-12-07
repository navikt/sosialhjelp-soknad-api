package no.nav.sbl.sosialhjelp.pdf;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;

import static no.nav.sbl.sosialhjelp.pdf.PDFFabrikk.lagPdfFil;

@Component
public class PDFService {

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private VedleggService vedleggService;

    public byte[] genererBrukerkvitteringPdf(JsonInternalSoknad internalSoknad, String servletPath, boolean erEttersending) {
        try {
            final String pdfMarkup = pdfTemplate.genererHtmlForPdf(internalSoknad, "/skjema/sosialhjelp/kvittering", erEttersending);
            return lagPdfFraMarkup(pdfMarkup, servletPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke generere brukerkvittering (Brukerkvittering.pdf).", e);
        }
    }
    
    public byte[] genererEttersendelsePdf(JsonInternalSoknad internalSoknad, String servletPath) {
        try {
            final String pdfMarkup = pdfTemplate.genererHtmlForPdf(internalSoknad, "skjema/ettersending/kvitteringUnderEttersendelse", true);
            return lagPdfFraMarkup(pdfMarkup, servletPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage hoveddokument for ettersendelse (ettersendelse.pdf).", e);
        }
    }
    
    public byte[] genereSaksbehandlerPdf(JsonInternalSoknad internalSoknad, String servletPath) {
        return genererOppsummeringPdf(internalSoknad, servletPath, false);
    }
    
    public byte[] genereJuridiskPdf(JsonInternalSoknad internalSoknad, String servletPath) {
        return genererOppsummeringPdf(internalSoknad, servletPath, true);
    }
    
    private byte[] genererOppsummeringPdf(JsonInternalSoknad internalSoknad, String servletPath, boolean fullSoknad) {
        try {
            final String pdfMarkup = pdfTemplate.genererHtmlForPdf(internalSoknad, fullSoknad);
            return lagPdfFraMarkup(pdfMarkup, servletPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage PDF for saksbehandler/juridisk. Fullsoknad: " + fullSoknad, e);
        }
    }
    
    public byte[] legacyGenererKvitteringPdf(WebSoknad soknad, String servletPath) {
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        final String hbsSkjemaPath = "/skjema/sosialhjelp/legacyKvittering";
        //innsending, ettersending
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }
    
    public byte[] legacyGenererEttersendingPdf(WebSoknad soknad, String servletPath) {
        final String hbsSkjemaPath = "skjema/ettersending/legacyKvitteringUnderEttersendelse";
        final String pdfMarkup; //ettersending, insnending
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    public byte[] legacyGenererOppsummeringPdf(WebSoknad soknad, String servletPath, boolean fullSoknad) {
        // oppsumemring saksbehandlerpdf, innsending
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, fullSoknad);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema", e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    private byte[] lagPdfFraMarkup(String pdfMarkup, String servletPath) {
        final byte[] pdf = lagPdfFil(pdfMarkup, servletPath);
        PdfValidator.softAssertValidPdfA(pdf);
        return pdf;
    }

}

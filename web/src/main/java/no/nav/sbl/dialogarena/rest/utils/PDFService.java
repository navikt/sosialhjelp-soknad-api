package no.nav.sbl.dialogarena.rest.utils;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.pdf.PdfWatermarker;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static no.nav.sbl.dialogarena.utils.PDFFabrikk.lagPdfFil;

@Component
public class PDFService {

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private WebSoknadConfig webSoknadConfig;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    private PdfWatermarker watermarker = new PdfWatermarker();

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
        final String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    public byte[] legacyGenererOppsummeringPdf(WebSoknad soknad, String servletPath, boolean fullSoknad) {
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
        String fnr = SubjectHandler.getSubjectHandler().getUid();
        byte[] pdf = lagPdfFil(pdfMarkup, servletPath);
        //pdf = watermarker.applyOn(pdf, fnr, true);
        return pdf;
    }

}

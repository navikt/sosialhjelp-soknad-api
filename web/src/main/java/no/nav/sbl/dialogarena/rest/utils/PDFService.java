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


    public byte[] genererKvitteringPdf(WebSoknad soknad, String servletPath, boolean erEttersending) {
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        String skjemanummer = soknad.getskjemaNummer();
        KravdialogInformasjon kravdialogInformasjon = kravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);
        final String hbsSkjemaPath = kravdialogInformasjon.getKvitteringTemplate();
        
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.genererHtmlForPdf(soknad, hbsSkjemaPath, erEttersending);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    public byte[] genererEttersendingPdf(WebSoknad soknad, String servletPath) {
        final String hbsSkjemaPath = "skjema/ettersending/kvitteringUnderEttersendelse";
        final String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.genererHtmlForPdf(soknad, hbsSkjemaPath, true);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    public byte[] genererOppsummeringPdf(WebSoknad soknad, String servletPath, boolean fullSoknad) {
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        if (webSoknadConfig.brukerNyOppsummering(soknad.getSoknadId())) {
            return lagPdf(soknad, servletPath, fullSoknad);
        } else {
            return lagPdfFraSkjema(soknad, "/skjema/" + soknad.getSoknadPrefix(), servletPath);
        }
    }


    private byte[] lagPdf(WebSoknad soknad, String servletPath, boolean fullSoknad) {
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.genererHtmlForPdf(soknad, fullSoknad);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema", e);
        }
        return lagPdfFraMarkup(pdfMarkup, servletPath);
    }

    private byte[] lagPdfFraSkjema(WebSoknad soknad, String hbsSkjemaPath, String servletPath) {
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
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

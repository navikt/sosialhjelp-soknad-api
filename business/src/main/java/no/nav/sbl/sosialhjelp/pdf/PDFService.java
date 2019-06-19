package no.nav.sbl.sosialhjelp.pdf;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
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
    private AdresseSystemdata adresseSystemdata;

    public byte[] genererBrukerkvitteringPdf(JsonInternalSoknad internalSoknad, String servletPath, boolean erEttersending, String eier) {
        try {
            final String pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(internalSoknad, "/skjema/kvittering/kvittering", erEttersending, eier);
            return lagPdfFraMarkup(pdfMarkup, servletPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke generere brukerkvittering (Brukerkvittering.pdf).", e);
        }
    }
    
    public byte[] genererEttersendelsePdf(JsonInternalSoknad internalSoknad, String servletPath, String eier) {
        try {
            final String pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(internalSoknad, "skjema/ettersending/kvitteringUnderEttersendelse", true, eier);
            return lagPdfFraMarkup(pdfMarkup, servletPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage hoveddokument for ettersendelse (ettersendelse.pdf).", e);
        }
    }
    
    public byte[] genererSaksbehandlerPdf(JsonInternalSoknad internalSoknad, String servletPath) {
        return genererOppsummeringPdf(internalSoknad, servletPath, false);
    }
    
    public byte[] genererJuridiskPdf(JsonInternalSoknad internalSoknad, String servletPath) {
        return genererOppsummeringPdf(internalSoknad, servletPath, true);
    }
    
    private byte[] genererOppsummeringPdf(JsonInternalSoknad internalSoknad, String servletPath, boolean fullSoknad) {
        try {
            String eier = internalSoknad.getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
            final String pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(internalSoknad, adresseSystemdata.innhentMidlertidigAdresse(eier), fullSoknad);
            return lagPdfFraMarkup(pdfMarkup, servletPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage PDF for saksbehandler/juridisk. Fullsoknad: " + fullSoknad, e);
        }
    }

    private byte[] lagPdfFraMarkup(String pdfMarkup, String servletPath) {
        final byte[] pdf = lagPdfFil(pdfMarkup, servletPath);
        PdfValidator.softAssertValidPdfA(pdf);
        return pdf;
    }

}

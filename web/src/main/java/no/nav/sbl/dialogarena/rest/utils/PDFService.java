package no.nav.sbl.dialogarena.rest.utils;

import no.nav.modig.core.context.*;
import no.nav.modig.core.exception.*;
import no.nav.sbl.dialogarena.pdf.*;
import no.nav.sbl.dialogarena.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import java.io.*;

import static no.nav.sbl.dialogarena.utils.PDFFabrikk.*;

@Component
public class PDFService {

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private VedleggService vedleggService;

    private PdfWatermarker watermarker = new PdfWatermarker();

    public byte[] genererPdfMedKodeverksverdier(WebSoknad soknad, String hbsSkjemaPath, String servletPath) {
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        return genererPdf(soknad, hbsSkjemaPath, servletPath);
    }

    public byte[] genererPdf(WebSoknad soknad, String hbsSkjemaPath, String servletPath) {
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }
        String fnr = SubjectHandler.getSubjectHandler().getUid();
        byte[] pdf = lagPdfFil(pdfMarkup, new File(servletPath).toURI().toString());
        pdf = watermarker.applyOn(pdf, fnr, true);
        return pdf;
    }

}

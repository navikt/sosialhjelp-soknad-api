package no.nav.sbl.dialogarena.rest.utils;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.utils.PDFFabrikk;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;

@Component
public class PDFService {

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private VedleggService vedleggService;

    @Context
    private ServletContext servletContext;

    public byte[] genererPdfMedKodeverksverdier(WebSoknad soknad, String hbsSkjemaPath) {
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        return genererPdf(soknad, hbsSkjemaPath);
    }

    public byte[] genererPdf(WebSoknad soknad, String hbsSkjemaPath) {
        String pdfMarkup;
        try {
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup for skjema " + hbsSkjemaPath, e);
        }

        String skjemaPath = new File(servletContext.getRealPath("/")).toURI().toString();

        return PDFFabrikk.lagPdfFil(pdfMarkup, skjemaPath);
    }


}

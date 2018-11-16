package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.rest.utils.PDFService;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.erFeatureAktiv;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.Toggle.RESSURS_FULLOPPSUMERING;

@Controller
@Path("/fulloppsummering")
public class FullOppsummeringRessurs {

    @Inject
    private SoknadDataFletter soknadDataFletter;
    @Inject
    private VedleggService vedleggService;
    @Inject
    private HtmlGenerator pdfTemplate;
    @Inject
    private PDFService pdfService;
    private static final Logger LOG = LoggerFactory.getLogger(FullOppsummeringRessurs.class);

    @Deprecated
    @GET
    @Path("/{behandlingsId}/nyoppsummering")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String hentOppsummeringNew(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        sjekkOmFullOppsummeringErAktivert("hentOppsummeringNew");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        return pdfTemplate.fyllHtmlMalMedInnhold(soknad);
    }
    
    @Deprecated
    @GET
    @Path("/{behandlingsId}/gammelfullsoknad")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String gammelFullsoknad(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        sjekkOmFullOppsummeringErAktivert("fullSoknad");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        return pdfTemplate.fyllHtmlMalMedInnhold(soknad, true);
    }

    @Deprecated
    @GET
    @Path("/{behandlingsId}/fullsoknad")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String fullSoknad(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        sjekkOmFullOppsummeringErAktivert("fullSoknad");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        return pdfTemplate.genererHtmlForPdf(soknad, true);
    }

    @Deprecated
    @GET
    @Path("/{behandlingsId}/fullsoknadpdf")
    @Produces("application/pdf")
    @SjekkTilgangTilSoknad
    public byte[] fullSoknadPdf(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) throws IOException {
        sjekkOmFullOppsummeringErAktivert("fullSoknadPdf");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        String servletPath = servletContext.getRealPath("/");
        return pdfService.genererOppsummeringPdf(soknad, servletPath, true);
    }
    
    @Deprecated
    @GET
    @Path("/{behandlingsId}/saksbehandler")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String saksbehandler(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        sjekkOmFullOppsummeringErAktivert("fullSoknad");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        return pdfTemplate.genererHtmlForPdf(soknad, false);
    }

    @Deprecated
    @GET
    @Path("/{behandlingsId}/saksbehandlerpdf")
    @Produces("application/pdf")
    @SjekkTilgangTilSoknad
    public byte[] saksbehandlerPdf(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) throws IOException {
        sjekkOmFullOppsummeringErAktivert("fullSoknadPdf");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        String servletPath = servletContext.getRealPath("/");
        return pdfService.genererOppsummeringPdf(soknad, servletPath, false);
    }


    @Deprecated
    @GET
    @Path("/{behandlingsId}/ettersendelsespdf")
    @Produces("application/pdf")
    @SjekkTilgangTilSoknad
    public byte[] ettersendelsepdf(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) throws IOException {
        sjekkOmFullOppsummeringErAktivert("ettersendelsepdf");
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        String servletPath = servletContext.getRealPath("/");
        return pdfService.genererEttersendingPdf(soknad, servletPath);
    }

    private void sjekkOmFullOppsummeringErAktivert(String metode) {
        LOG.warn("OppsummeringRessurs metode {} forsøkt aksessert", metode);
        if (!erFeatureAktiv(RESSURS_FULLOPPSUMERING)) {
            throw new NotFoundException("Ikke aktivert fulloppsummering");
        }
    }

}

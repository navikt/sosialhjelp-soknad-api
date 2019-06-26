package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.AdresseSystemdata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.Toggle.RESSURS_FULLOPPSUMERING;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.erFeatureAktiv;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/fulloppsummering")
public class FullOppsummeringRessurs {

    @Inject
    private HtmlGenerator pdfTemplate;
    @Inject
    private PDFService pdfService;
    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Inject
    private AdresseSystemdata adresseSystemdata;
    private static final Logger LOG = LoggerFactory.getLogger(FullOppsummeringRessurs.class);

    @GET
    @Path("/{behandlingsId}/nyoppsummering")
    @Produces(TEXT_HTML)
    public String hentOppsummeringNew(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        sjekkOmFullOppsummeringErAktivert("hentOppsummeringNew");

        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        return pdfTemplate.fyllHtmlMalMedInnhold(soknadUnderArbeid.getJsonInternalSoknad(), adresseSystemdata.innhentMidlertidigAdresse(eier));
    }

    @GET
    @Path("/{behandlingsId}/fullsoknadpdf")
    @Produces("application/pdf")
    public byte[] fullSoknadPdf(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) {
        sjekkOmFullOppsummeringErAktivert("fullSoknadPdf");

        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        return pdfService.genererJuridiskPdf(soknadUnderArbeid.getJsonInternalSoknad(), "/");
    }

    private void sjekkOmFullOppsummeringErAktivert(String metode) {
        LOG.warn("OppsummeringRessurs metode {} forsøkt aksessert", metode);
        if (!erFeatureAktiv(RESSURS_FULLOPPSUMERING)) {
            throw new NotFoundException("Ikke aktivert fulloppsummering");
        }
    }
}

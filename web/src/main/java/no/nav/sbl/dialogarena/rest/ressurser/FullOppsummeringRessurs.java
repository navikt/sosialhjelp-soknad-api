package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggOriginalFilerService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
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
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.erFeatureAktiv;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.FeatureToggler.Toggle.RESSURS_FULLOPPSUMERING;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
    @Inject
    private WebSoknadConverter webSoknadConverter;
    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;
    @Inject
    private WebSoknadConfig webSoknadConfig;
    @Inject
    private VedleggOriginalFilerService vedleggOriginalFilerService;
    private static final Logger LOG = LoggerFactory.getLogger(FullOppsummeringRessurs.class);

    @GET
    @Path("/{behandlingsId}/nyoppsummering")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String hentOppsummeringNew(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        sjekkOmFullOppsummeringErAktivert("hentOppsummeringNew");
        vedleggOriginalFilerService.oppdaterVedleggOgBelopFaktum(behandlingsId);
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(webSoknadConfig.hentStruktur(soknad.getskjemaNummer()));
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        final SoknadUnderArbeid soknadUnderArbeid = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(soknad, true);
        return pdfTemplate.fyllHtmlMalMedInnhold(soknadUnderArbeid.getJsonInternalSoknad());
    }

    @GET
    @Path("/{behandlingsId}/fullsoknadpdf")
    @Produces("application/pdf")
    @SjekkTilgangTilSoknad
    public byte[] fullSoknadPdf(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) throws IOException {
        sjekkOmFullOppsummeringErAktivert("fullSoknadPdf");
        vedleggOriginalFilerService.oppdaterVedleggOgBelopFaktum(behandlingsId);
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(webSoknadConfig.hentStruktur(soknad.getskjemaNummer()));
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        final SoknadUnderArbeid soknadUnderArbeid = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(soknad, true);

        return pdfService.genererJuridiskPdf(soknadUnderArbeid.getJsonInternalSoknad(), "/");
    }

    private void sjekkOmFullOppsummeringErAktivert(String metode) {
        LOG.warn("OppsummeringRessurs metode {} forsøkt aksessert", metode);
        if (!erFeatureAktiv(RESSURS_FULLOPPSUMERING)) {
            throw new NotFoundException("Ikke aktivert fulloppsummering");
        }
    }
}

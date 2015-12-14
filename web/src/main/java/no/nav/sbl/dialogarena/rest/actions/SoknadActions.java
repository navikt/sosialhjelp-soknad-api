package no.nav.sbl.dialogarena.rest.actions;

import no.nav.sbl.dialogarena.rest.meldinger.*;
import no.nav.sbl.dialogarena.rest.utils.*;
import no.nav.sbl.dialogarena.service.*;
import no.nav.sbl.dialogarena.sikkerhet.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import org.slf4j.*;

import javax.inject.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

import static javax.ws.rs.core.MediaType.*;
import static no.nav.sbl.dialogarena.utils.UrlUtils.*;

@Path("/soknader/{behandlingsId}/actions")
@Produces(APPLICATION_JSON)
public class SoknadActions {

    private static Logger logger = LoggerFactory.getLogger(SoknadActions.class);

    private String saksoversiktUrl = System.getProperty("saksoversikt.link.url");

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private PDFService pdfService;

    @Inject
    private EmailService emailService;

    @Inject
    private NavMessageSource tekster;

    @Inject
    WebSoknadConfig webSoknadConfig;

    @Context
    private ServletContext servletContext;

    @GET
    @Path("/leggved")
    @SjekkTilgangTilSoknad
    public Vedlegg leggVedVedlegg(@PathParam("behandlingsId") final String behandlingsId, @QueryParam("vedleggId") final Long vedleggId) {
        vedleggService.genererVedleggFaktum(behandlingsId, vedleggId);
        return vedleggService.hentVedlegg(vedleggId);
    }

    @POST
    @Path("/send")
    @SjekkTilgangTilSoknad
    public void sendSoknad(@PathParam("behandlingsId") String behandlingsId) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);

        byte[] kvittering = pdfService.genererPdfMedKodeverksverdier(soknad, "/skjema/kvittering", servletContext.getRealPath("/"));
        vedleggService.lagreKvitteringSomVedlegg(behandlingsId, kvittering);

        if (soknad.erEttersending()) {
            byte[] dummyPdfSomHovedskjema = pdfService.genererPdf(soknad, "skjema/ettersending/dummy", servletContext.getRealPath("/"));
            soknadService.sendSoknad(behandlingsId, dummyPdfSomHovedskjema);
        } else {
            String pdfTemplate = webSoknadConfig.brukerNyOppsummering(soknad.getSoknadId()) ? "generisk" : soknad.getSoknadPrefix();
            byte[] soknadPdf = pdfService.genererPdfMedKodeverksverdier(soknad, "/skjema/" + pdfTemplate, servletContext.getRealPath("/"));
            soknadService.sendSoknad(behandlingsId, soknadPdf);
        }
    }

    @POST
    @Path("/fortsettsenere")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, FortsettSenere epost, @Context HttpServletRequest request) {
        String content = tekster.finnTekst("fortsettSenere.sendEpost.epostInnhold", new Object[]{getFortsettUrl(behandlingsId)}, new Locale("nb", "NO"));
        String subject = tekster.finnTekst("fortsettSenere.sendEpost.epostTittel", null, new Locale("nb", "NO"));

        emailService.sendEpost(epost.getEpost(), subject, content, behandlingsId);
    }

    @POST
    @Path("/bekreftinnsending")
    @SjekkTilgangTilSoknad
    public void sendEpost(@PathParam("behandlingsId") String behandlingsId, SoknadBekreftelse soknadBekreftelse, @Context HttpServletRequest request) {
        if (soknadBekreftelse.getEpost() != null && !soknadBekreftelse.getEpost().isEmpty()) {
            String subject = tekster.finnTekst("sendtSoknad.sendEpost.epostSubject", null, new Locale("nb", "NO"));
            String ettersendelseUrl = getEttersendelseUrl(request.getRequestURL().toString(), behandlingsId);
            String saksoversiktLink = saksoversiktUrl + "/detaljer/" + soknadBekreftelse.getTemaKode() + "/" + behandlingsId;

            String innhold;
            if (soknadBekreftelse.getErEttersendelse()) {
                innhold = tekster.finnTekst("sendEttersendelse.sendEpost.epostInnhold", new Object[]{saksoversiktLink}, new Locale("nb", "NO"));
            } else {
                innhold = tekster.finnTekst("sendtSoknad.sendEpost.epostInnhold", new Object[]{saksoversiktLink, ettersendelseUrl}, new Locale("nb", "NO"));
            }

            emailService.sendEpost(soknadBekreftelse.getEpost(), subject, innhold, behandlingsId);
        } else {
            logger.debug("Fant ingen epostadresse");
        }
    }

    @GET
    @Path("/opprinneliginnsendtdato")
    @Produces(TEXT_PLAIN)
    @SjekkTilgangTilSoknad
    public Long finnOpprinneligInnsendtDato(@PathParam("behandlingsId") String behandlingsId) {
        return soknadService.hentOpprinneligInnsendtDato(behandlingsId);
    }

    @GET
    @Path("/sistinnsendtebehandlingsid")
    @Produces(TEXT_PLAIN)
    @SjekkTilgangTilSoknad
    public String finnSisteInnsendteBehandlingsId(@PathParam("behandlingsId") String behandlingsId) {
        return soknadService.hentSisteInnsendteBehandlingsId(behandlingsId);
    }

    void setContext(ServletContext context) {
        servletContext = context;
    }

}

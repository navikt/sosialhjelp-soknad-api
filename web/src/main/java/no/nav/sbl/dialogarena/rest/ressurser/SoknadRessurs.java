package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Henvendelse;
import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.generateXsrfToken;

@Controller
@Path("/soknader")
@Timed
@Produces(APPLICATION_JSON)
public class SoknadRessurs {

    public static final String XSRF_TOKEN = "XSRF-TOKEN-SOKNAD-API";
    private static final Logger LOG = LoggerFactory.getLogger(SoknadRessurs.class);

    @Inject
    private FaktaService faktaService;

    @Inject
    private VedleggService vedleggService;

    @Inject
    private SoknadService soknadService;

    @Inject
    private InnsendtSoknadService innsendtSoknadService;

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private WebSoknadConfig webSoknadConfig;

    @GET
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public WebSoknad hentSoknadData(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletResponse response) {
        response.addCookie(xsrfCookie(behandlingsId));
        return soknadService.hentSoknad(behandlingsId, true, false);
    }

    @GET
    @Path("/{behandlingsId}")
    @Produces("application/vnd.kvitteringforinnsendtsoknad+json")
    @SjekkTilgangTilSoknad(type = Henvendelse)
    public InnsendtSoknad hentInnsendtSoknad(@PathParam("behandlingsId") String behandlingsId, @QueryParam("sprak") String sprak) {
        InnsendtSoknad innsendtSoknad = innsendtSoknadService.hentInnsendtSoknad(behandlingsId, sprak);
        if (innsendtSoknad.getTemakode().equals("DAG")) {
            Event event = MetricsFactory.createEvent("soknad.innsendingsstatistikk");
            if (innsendtSoknad.getIkkeInnsendteVedlegg().isEmpty()) {
                event.addTagToReport("soknad.innsendingsstatistikk.komplett", "true");
            }
            else {
                LOG.info("Else kjører!");
                event.addTagToReport("soknad.innsendingsstatistikk.komplett", "false");
                List<Vedlegg> ikkeInnsendteVedlegg = innsendtSoknad.getIkkeInnsendteVedlegg();
                /*
                for (Vedlegg vedlegg : ikkeInnsendteVedlegg) {
                    event.addFieldToReport("vedlegg."+vedlegg.getSkjemaNummer(), vedlegg.getTittel());
                    event.addFieldToReport("vedlegg."+vedlegg.getSkjemaNummer(), vedlegg.getInnsendingsvalg());
                }
                */
            }
            event.report();
        }
        return innsendtSoknad;
    }

    /*
    * Denne metoden er deprecated og erstattet av en lik metode med egen mediatype.
    * I utgangspunktet skal den nye mediatypen i produksjon i HL2-2016.
    * Etter en stund i prod (feks en måned) kan man sjekke accesslogger og se om dette endepunktet med mediatype text/html fortsatt er i bruk
    * Som fallback ettersom vi er usikre på om accessloggene indekseres, har jeg lagt inn et logstatment med en warning.
    * Dersom man ser at metoden ikke er i bruk, kan denne den slettes.
    * */
    @Deprecated
    @GET
    @Path("/{behandlingsId}")
    @Produces(MediaType.TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String hentOppsummeringMedStandardMediatype(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        LOG.warn("Bruk av deprecated metode for å hente oppsummering");
        return hentOppsummering(behandlingsId);
    }

    @GET
    @Path("/{behandlingsId}")
    @Produces("application/vnd.oppsummering+html")
    @SjekkTilgangTilSoknad
    public String hentOppsummering(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        if (webSoknadConfig.brukerNyOppsummering(soknad.getSoknadId())) {
            return pdfTemplate.fyllHtmlMalMedInnhold(soknad);
        }
        return pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/" + soknad.getSoknadPrefix());
    }


    @POST
    @Consumes(APPLICATION_JSON)
    public Map<String, String> opprettSoknad(@QueryParam("ettersendTil") String behandlingsId, StartSoknad soknadType, @Context HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        String opprettetBehandlingsId;
        if (behandlingsId == null) {
            opprettetBehandlingsId = soknadService.startSoknad(soknadType.getSoknadType());
        } else {
            WebSoknad soknad = soknadService.hentEttersendingForBehandlingskjedeId(behandlingsId);
            if (soknad == null) {
                opprettetBehandlingsId = soknadService.startEttersending(behandlingsId);
            } else {
                opprettetBehandlingsId = soknad.getBrukerBehandlingId();
            }
        }
        result.put("brukerBehandlingId", opprettetBehandlingsId);
        response.addCookie(xsrfCookie(opprettetBehandlingsId));
        return result;
    }

    @PUT  //TODO: Burde endres til å sende med hele objektet for å følge spec'en
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public void oppdaterSoknad(@PathParam("behandlingsId") String behandlingsId,
                               @QueryParam("delsteg") String delsteg,
                               @QueryParam("journalforendeenhet") String journalforendeenhet) {

        if (delsteg == null && journalforendeenhet == null) {
            throw new BadRequestException("Ingen queryparametre ble sendt inn.");
        }

        if (delsteg != null) {
            settDelstegStatus(behandlingsId, delsteg);
        }

        if (journalforendeenhet != null) {
            settJournalforendeEnhet(behandlingsId, journalforendeenhet);
        }
    }

    @DELETE
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public void slettSoknad(@PathParam("behandlingsId") String behandlingsId) {
        soknadService.avbrytSoknad(behandlingsId);
    }

    @GET
    @Path("/{behandlingsId}/fakta")
    @SjekkTilgangTilSoknad
    public List<Faktum> hentFakta(@PathParam("behandlingsId") String behandlingsId) {
        return faktaService.hentFakta(behandlingsId);
    }

    @GET
    @Path("/{behandlingsId}/vedlegg")
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentPaakrevdeVedlegg(@PathParam("behandlingsId") String behandlingsId) {
        return vedleggService.hentPaakrevdeVedlegg(behandlingsId);
    }

    private void settJournalforendeEnhet(String behandlingsId, String delsteg) {
        soknadService.settJournalforendeEnhet(behandlingsId, delsteg);
    }

    private void settDelstegStatus(String behandlingsId, String delsteg) {
        DelstegStatus delstegstatus;
        if (delsteg.equalsIgnoreCase("utfylling")) {
            delstegstatus = DelstegStatus.UTFYLLING;

        } else if (delsteg.equalsIgnoreCase("opprettet")) {
            delstegstatus = DelstegStatus.OPPRETTET;

        } else if (delsteg.equalsIgnoreCase("vedlegg")) {
            delstegstatus = DelstegStatus.SKJEMA_VALIDERT;

        } else if (delsteg.equalsIgnoreCase("oppsummering")) {
            delstegstatus = DelstegStatus.VEDLEGG_VALIDERT;

        } else {
            throw new ApplicationException("Ugyldig delsteg sendt inn til REST-controller.");
        }
        soknadService.settDelsteg(behandlingsId, delstegstatus);
    }

    private static Cookie xsrfCookie(String behandlingId) {
        Cookie xsrfCookie = new Cookie(XSRF_TOKEN, generateXsrfToken(behandlingId));
        xsrfCookie.setPath("/");
        xsrfCookie.setSecure(true);
        return xsrfCookie;
    }

}

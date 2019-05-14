package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggOriginalFilerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SynligeFaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
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
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.generateXsrfToken;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
    private HtmlGenerator pdfTemplate;

    @Inject
    private WebSoknadConfig webSoknadConfig;

    @Inject
    private SynligeFaktaService synligeFaktaService;

    @Inject
    private WebSoknadConverter webSoknadConverter;

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Inject
    private VedleggOriginalFilerService vedleggOriginalFilerService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SystemdataUpdater systemdata;

    @GET
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public WebSoknad hentSoknadData(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletResponse response) {
        response.addCookie(xsrfCookie(behandlingsId));
        return soknadService.hentSoknad(behandlingsId, true, false);
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
        vedleggOriginalFilerService.oppdaterVedleggOgBelopFaktum(behandlingsId);
        vedleggService.hentPaakrevdeVedlegg(behandlingsId);
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);
        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(webSoknadConfig.hentStruktur(soknad.getskjemaNummer()));
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        final SoknadUnderArbeid konvertertSoknadUnderArbeid = webSoknadConverter.mapWebSoknadTilSoknadUnderArbeid(soknad, true);

        return pdfTemplate.fyllHtmlMalMedInnhold(konvertertSoknadUnderArbeid.getJsonInternalSoknad());
    }

    @GET
    @Path("/{behandlingsId}/erSystemdataEndret")
    public boolean sjekkOmSystemdataErEndret(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        systemdata.update(soknadUnderArbeid);

        final JsonInternalSoknad updatedJsonInternalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        final JsonInternalSoknad notUpdatedJsonInternalSoknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get().getJsonInternalSoknad();

        if (updatedJsonInternalSoknad.equals(notUpdatedJsonInternalSoknad)){
            return false;
        } else {
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
            return true;
        }
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Map<String, String> legacyOpprettSoknad(@QueryParam("ettersendTil") String behandlingsId, StartSoknad soknadType, @Context HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        String opprettetBehandlingsId;
        if (behandlingsId == null) {
            opprettetBehandlingsId = soknadService.startSoknad(soknadType.getSoknadType());
        } else {
            WebSoknad soknad = soknadService.hentEttersendingForBehandlingskjedeId(behandlingsId);
            if (soknad == null) {
                opprettetBehandlingsId = soknadService.legacyStartEttersending(behandlingsId);
            } else {
                opprettetBehandlingsId = soknad.getBrukerBehandlingId();
            }
        }
        result.put("brukerBehandlingId", opprettetBehandlingsId);
        response.addCookie(xsrfCookie(opprettetBehandlingsId));
        return result;
    }

    @POST
    @Path("/opprettSoknad")
    @Consumes(APPLICATION_JSON)
    public Map<String, String> opprettSoknad(@QueryParam("ettersendTil") String behandlingsId, StartSoknad soknadType, @Context HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        String opprettetBehandlingsId;
        if (behandlingsId == null) {
            opprettetBehandlingsId = soknadService.startSoknad(soknadType.getSoknadType());
        } else {
            final String eier = OidcFeatureToggleUtils.getUserId();
            WebSoknad soknad = soknadService.hentEttersendingForBehandlingskjedeId(behandlingsId);
            if (soknad == null){
                Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(behandlingsId, eier);
                if (soknadUnderArbeid.isPresent()) {
                    opprettetBehandlingsId = soknadUnderArbeid.get().getBehandlingsId();
                } else {
                    opprettetBehandlingsId = soknadService.startEttersending(behandlingsId);
                }
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

    @PUT
    @Path("/{behandlingsId}/fakta")
    @SjekkTilgangTilSoknad
    public void lagreFakta(@PathParam("behandlingsId") String behandlingsId, WebSoknad soknad) {
        soknad.getFakta().stream().forEach(faktum -> faktaService.lagreBrukerFaktum(faktum));
    }

    @GET
    @Path("/{behandlingsId}/vedlegg")
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentPaakrevdeVedlegg(@PathParam("behandlingsId") String behandlingsId) {
        return vedleggService.hentPaakrevdeVedlegg(behandlingsId);
    }

    @GET
    @Path("/{behandlingsId}/synligsoknadstruktur")
    @SjekkTilgangTilSoknad
    public List<FaktumStruktur> hentSynligSoknadStruktur(@PathParam("behandlingsId") String behandlingsId, @QueryParam("panelFilter") String filter) {
        return synligeFaktaService.finnSynligeFaktaForSoknad(behandlingsId, filter);
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

package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdf.HtmlGenerator;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashMap;
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

    @Inject
    private SoknadService soknadService;

    @Inject
    private HtmlGenerator pdfTemplate;

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SystemdataUpdater systemdata;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @GET
    @Path("/{behandlingsId}")
    @Produces("application/vnd.oppsummering+html")
    public String hentOppsummering(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();

        return pdfTemplate.fyllHtmlMalMedInnhold(soknadUnderArbeid.getJsonInternalSoknad());
    }

    @GET
    @Path("/{behandlingsId}/erSystemdataEndret")
    public boolean sjekkOmSystemdataErEndret(@PathParam("behandlingsId") String behandlingsId) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        systemdata.update(soknadUnderArbeid);

        final JsonInternalSoknad updatedJsonInternalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        SoknadUnderArbeid notUpdatedSoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonInternalSoknad notUpdatedJsonInternalSoknad = notUpdatedSoknadUnderArbeid.getJsonInternalSoknad();

        soknadUnderArbeidService.sortOkonomi(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi());
        soknadUnderArbeidService.sortOkonomi(notUpdatedSoknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi());

        if (updatedJsonInternalSoknad.equals(notUpdatedJsonInternalSoknad)){
            return false;
        } else {
            soknadUnderArbeidService.logDifferences(notUpdatedSoknadUnderArbeid, soknadUnderArbeid, "Forskjell på systemdata i json: ");
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknadUnderArbeid, eier);
            return true;
        }
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Map<String, String> legacyOpprettSoknad(@QueryParam("ettersendTil") String behandlingsId, StartSoknad soknadType, @Context HttpServletResponse response) {
        return opprettSoknad(behandlingsId, response);
    }

    @POST
    @Path("/opprettSoknad")
    @Consumes(APPLICATION_JSON)
    public Map<String, String> opprettSoknad(@QueryParam("ettersendTil") String behandlingsId, @Context HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        String opprettetBehandlingsId;
        if (behandlingsId == null) {
            opprettetBehandlingsId = soknadService.startSoknad();
        } else {
            final String eier = OidcFeatureToggleUtils.getUserId();
            Optional<SoknadUnderArbeid> soknadUnderArbeid = soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(behandlingsId, eier);
            if (soknadUnderArbeid.isPresent()) {
                opprettetBehandlingsId = soknadUnderArbeid.get().getBehandlingsId();
            } else {
                opprettetBehandlingsId = soknadService.startEttersending(behandlingsId);
            }
        }
        result.put("brukerBehandlingId", opprettetBehandlingsId);
        response.addCookie(xsrfCookie(opprettetBehandlingsId));
        return result;
    }

    @DELETE
    @Path("/{behandlingsId}")
    public void slettSoknad(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        soknadService.avbrytSoknad(behandlingsId);
    }

    private static Cookie xsrfCookie(String behandlingId) {
        Cookie xsrfCookie = new Cookie(XSRF_TOKEN, generateXsrfToken(behandlingId));
        xsrfCookie.setPath("/");
        xsrfCookie.setSecure(true);
        return xsrfCookie;
    }

}

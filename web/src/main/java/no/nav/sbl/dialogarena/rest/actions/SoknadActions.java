package no.nav.sbl.dialogarena.rest.actions;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.DigisosApi;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.KommuneStatus;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/soknader/{behandlingsId}/actions")
@Produces(APPLICATION_JSON)
@Timed(name = "SoknadActionsRessurs")
public class SoknadActions {

    private static final Logger log = getLogger(SoknadActions.class);

    @Inject
    private SoknadService soknadService;

    @Inject
    private DigisosApi digisosApi;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @POST
    @Path("/send")
    public void sendSoknad(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext, @HeaderParam(value = AUTHORIZATION) String token) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        if (soknadUnderArbeid != null) {
            // String kommunenummer = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getMottaker().getKommunenummer();
            try {
                KommuneStatus kommuneStatus = digisosApi.kommuneInfo("1201");
                log.info(String.format("Kommune: %s Status: %s", "1201", kommuneStatus.name()));
            } catch (Exception e) {
                log.error("Feil ved henting av kommuneinfo ", e);
            }
//            if ((kommuneStatus != KommuneStatus.IKKE_PA_FIKS_ELLER_INNSYN)&& false) {
//                digisosApiService.sendSoknad(soknadUnderArbeid, token, kommunenummer);
//                return;
//            }
        }

        soknadService.sendSoknad(behandlingsId);
    }
}

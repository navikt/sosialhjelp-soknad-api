package no.nav.sbl.dialogarena.rest.actions;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/actions")
@Produces(APPLICATION_JSON)
@Timed(name = "SoknadActionsRessurs")
public class SoknadActions {

    @Inject
    private SoknadService soknadService;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @POST
    @Path("/send")
    public void sendSoknad(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        soknadService.sendSoknad(behandlingsId);
    }
}

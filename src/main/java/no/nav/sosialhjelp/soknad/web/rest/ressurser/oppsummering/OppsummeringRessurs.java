package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.service.oppsummering.OppsummeringService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Oppsummering;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/soknader/{behandlingsId}/oppsummering")
@Timed
@Produces(APPLICATION_JSON)
public class OppsummeringRessurs {

    private static final Logger log = getLogger(OppsummeringRessurs.class);

    private final OppsummeringService oppsummeringService;
    private final no.nav.sosialhjelp.soknad.oppsummering.OppsummeringService kotlinOppsummering;
    private final Tilgangskontroll tilgangskontroll;

    public OppsummeringRessurs(
            OppsummeringService oppsummeringService,
            no.nav.sosialhjelp.soknad.oppsummering.OppsummeringService kotlinOppsummering,
            Tilgangskontroll tilgangskontroll
    ) {
        this.oppsummeringService = oppsummeringService;
        this.kotlinOppsummering = kotlinOppsummering;
        this.tilgangskontroll = tilgangskontroll;
    }

    @GET
    public Oppsummering getOppsummering(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId);
        var eier = SubjectHandler.getUserId();

        try {
            kotlinOppsummering.hentOppsummering(eier, behandlingsId);
        } catch (Exception e) {
            log.warn("noe feilet med ny oppsummeringsside / kotlin", e);
        }

        return oppsummeringService.hentOppsummering(eier, behandlingsId);
    }
}



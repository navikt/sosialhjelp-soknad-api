package no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.service.dialog.SistInnsendteSoknadService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog.dto.SistInnsendteSoknadDto;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.TOKENX;

@Controller
@ProtectedWithClaims(issuer = TOKENX, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/dialog")
@Produces(APPLICATION_JSON)
@Timed
public class SistInnsendteSoknadRessurs {

    private final SistInnsendteSoknadService nyligInnsendteSoknaderService;

    public SistInnsendteSoknadRessurs(
            SistInnsendteSoknadService nyligInnsendteSoknaderService
    ) {
        this.nyligInnsendteSoknaderService = nyligInnsendteSoknaderService;
    }

    @GET
    @Path("/sistInnsendteSoknad")
    public SistInnsendteSoknadDto hentNyligInnsendteSoknader() {
        var fnr = SubjectHandler.getUserId();
        return nyligInnsendteSoknaderService.hentSistInnsendteSoknad(fnr).orElse(null);
    }
}

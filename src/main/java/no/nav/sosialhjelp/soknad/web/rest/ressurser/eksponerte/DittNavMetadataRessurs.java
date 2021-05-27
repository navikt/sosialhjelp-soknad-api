package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.service.dittnav.DittNavMetadataService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.PabegyntSoknadDto;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_3;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4})
@Path("/dittnav")
@Timed
@Produces(APPLICATION_JSON)
public class DittNavMetadataRessurs {

    private static final Logger log = getLogger(DittNavMetadataRessurs.class);

    private final DittNavMetadataService dittNavMetadataService;

    public DittNavMetadataRessurs(DittNavMetadataService dittNavMetadataService) {
        this.dittNavMetadataService = dittNavMetadataService;
    }

    @GET
    @Path("/pabegynte")
    public List<PabegyntSoknadDto> hentPabegynteSoknaderForBruker() {
        var fnr = SubjectHandler.getUserId();
        return dittNavMetadataService.hentPabegynteSoknader(fnr);
    }

    @GET
    @Unprotected
    @Path("/ping")
    public String ping() {
        log.debug("Ping for DittNav");
        return "pong";
    }
}

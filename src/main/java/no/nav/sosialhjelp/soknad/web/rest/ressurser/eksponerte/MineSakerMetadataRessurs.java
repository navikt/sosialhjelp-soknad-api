package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.service.minesaker.MineSakerMetadataService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto.InnsendtSoknadDto;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.TOKENX;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
@ProtectedWithClaims(issuer = TOKENX, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/minesaker")
@Timed
@Produces(APPLICATION_JSON)
public class MineSakerMetadataRessurs {

    private static final Logger log = getLogger(MineSakerMetadataRessurs.class);

    private final MineSakerMetadataService mineSakerMetadataService;

    public MineSakerMetadataRessurs(
            MineSakerMetadataService mineSakerMetadataService
    ) {
        this.mineSakerMetadataService = mineSakerMetadataService;
    }

    /**
     * Henter informasjon om innsendte søknader via SoknadMetadataRepository.
     * På sikt vil vi hente denne informasjonen fra Fiks (endepunkt vil da høre mer hjemme i innsyn-api)
     */
    @GET
    @Path("/innsendte")
    public List<InnsendtSoknadDto> hentInnsendteSoknaderForBruker() {
        var fnr = SubjectHandler.getUserId();

        return mineSakerMetadataService.hentInnsendteSoknader(fnr);
    }

    @GET
    @Unprotected
    @Path("/ping")
    public String ping() {
        log.debug("Ping for MineSaker");
        return "pong";
    }
}

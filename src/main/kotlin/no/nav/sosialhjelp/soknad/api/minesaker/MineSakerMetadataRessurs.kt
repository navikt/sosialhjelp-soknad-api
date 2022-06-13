package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.api.minesaker.dto.InnsendtSoknadDto
import no.nav.sosialhjelp.soknad.common.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.common.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.common.Constants.TOKENX
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = TOKENX, combineWithOr = true, claimMap = [CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4])
@Path("/minesaker")
@Produces(MediaType.APPLICATION_JSON)
open class MineSakerMetadataRessurs(
    private val mineSakerMetadataService: MineSakerMetadataService
) {
    /**
     * Henter informasjon om innsendte søknader via SoknadMetadataRepository.
     * På sikt vil vi hente denne informasjonen fra Fiks (endepunkt vil da høre mer hjemme i innsyn-api)
     */
    @GET
    @Path("/innsendte")
    open fun hentInnsendteSoknaderForBruker(): List<InnsendtSoknadDto> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return mineSakerMetadataService.hentInnsendteSoknader(fnr)
    }

    @GET
    @Unprotected
    @Path("/ping")
    open fun ping(): String {
        log.debug("Ping for MineSaker")
        return "pong"
    }

    companion object {
        private val log = LoggerFactory.getLogger(MineSakerMetadataRessurs::class.java)
    }
}

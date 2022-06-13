package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.innsyn.dto.SoknadOversiktDto
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknadoversikt")
@Produces(MediaType.APPLICATION_JSON)
open class SoknadOversiktRessurs(
    private val service: SoknadOversiktService,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GET
    @Path("/soknader")
    open fun hentInnsendteSoknaderForBruker(): List<SoknadOversiktDto> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        logger.debug("Henter alle søknader")
        val soknader: List<SoknadOversiktDto> = service.hentSvarUtSoknaderFor(fnr)
        logger.debug("Hentet {} søknader for bruker", soknader.size)
        return soknader
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SoknadOversiktRessurs::class.java)
    }
}

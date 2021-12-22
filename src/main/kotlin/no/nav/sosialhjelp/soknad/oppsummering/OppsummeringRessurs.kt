package no.nav.sosialhjelp.soknad.oppsummering

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.oppsummering.dto.Oppsummering
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/oppsummering")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class OppsummeringRessurs(
    private val oppsummeringService: OppsummeringService,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GET
    open fun getOppsummering(@PathParam("behandlingsId") behandlingsId: String): Oppsummering {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        return oppsummeringService.hentOppsummering(eier, behandlingsId)
    }
}

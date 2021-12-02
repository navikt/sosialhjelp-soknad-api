package no.nav.sosialhjelp.soknad.api.dialog

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.api.dialog.dto.SistInnsendteSoknadDto
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/dialog")
@Produces(MediaType.APPLICATION_JSON)
@Timed
open class SistInnsendteSoknadRessurs(
    private val sistInnsendteSoknadService: SistInnsendteSoknadService
) {
    @GET
    @Path("/sistInnsendteSoknad")
    open fun hentSistInnsendteSoknad(): SistInnsendteSoknadDto? {
        val fnr = SubjectHandler.getUserId()
        return sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
    }
}

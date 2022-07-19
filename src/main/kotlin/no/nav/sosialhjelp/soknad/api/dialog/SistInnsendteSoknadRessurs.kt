package no.nav.sosialhjelp.soknad.api.dialog

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.dialog.dto.SistInnsendteSoknadDto
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.app.Constants.TOKENX
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = TOKENX, claimMap = [CLAIM_ACR_LEVEL_4])
@Path("/dialog")
@Produces(MediaType.APPLICATION_JSON)
open class SistInnsendteSoknadRessurs(
    private val sistInnsendteSoknadService: SistInnsendteSoknadService
) {
    @GET
    @Path("/sistInnsendteSoknad")
    open fun hentSistInnsendteSoknad(): SistInnsendteSoknadDto? {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return sistInnsendteSoknadService.hentSistInnsendteSoknad(fnr)
    }
}

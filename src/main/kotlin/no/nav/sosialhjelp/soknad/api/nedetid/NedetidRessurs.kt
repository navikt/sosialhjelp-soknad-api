package no.nav.sosialhjelp.soknad.api.nedetid

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.api.nedetid.dto.NedetidFrontend
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@Unprotected
@Path("/nedetid")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class NedetidRessurs(
    private val nedetidService: NedetidService
) {

    @GET
    open fun hentNedetidInformasjon(): NedetidFrontend {
        return NedetidFrontend(
            isNedetid = nedetidService.isInnenforNedetid,
            isPlanlagtNedetid = nedetidService.isInnenforPlanlagtNedetid,
            nedetidStart = nedetidService.nedetidStartAsString,
            nedetidSlutt = nedetidService.nedetidSluttAsString,
            nedetidStartText = nedetidService.nedetidStartAsHumanReadable,
            nedetidSluttText = nedetidService.nedetidSluttAsHumanReadable,
            nedetidStartTextEn = nedetidService.nedetidStartAsHumanReadableEn,
            nedetidSluttTextEn = nedetidService.nedetidSluttAsHumanReadableEn
        )
    }
}

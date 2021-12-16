package no.nav.sosialhjelp.soknad.api.nedetid

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils.NEDETID_SLUTT
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils.NEDETID_START
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
open class NedetidRessurs {

    @GET
    open fun hentNedetidInformasjon(): NedetidFrontend {
        return NedetidFrontend(
            isNedetid = NedetidUtils.isInnenforNedetid,
            isPlanlagtNedetid = NedetidUtils.isInnenforPlanlagtNedetid,
            nedetidStart = NedetidUtils.getNedetidAsStringOrNull(NEDETID_START),
            nedetidSlutt = NedetidUtils.getNedetidAsStringOrNull(NEDETID_SLUTT),
            nedetidStartText = NedetidUtils.getNedetidAsHumanReadable(NEDETID_START),
            nedetidSluttText = NedetidUtils.getNedetidAsHumanReadable(NEDETID_SLUTT),
            nedetidStartTextEn = NedetidUtils.getNedetidAsHumanReadableEn(NEDETID_START),
            nedetidSluttTextEn = NedetidUtils.getNedetidAsHumanReadableEn(NEDETID_SLUTT)
        )
    }
}

package no.nav.sosialhjelp.soknad.app.health

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.selftest.SelftestResult
import no.nav.sosialhjelp.selftest.SelftestService
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@Unprotected
@Path("/internal")
open class InternalRessurs(
    private val selftestService: SelftestService
) {

    @Produces(MediaType.TEXT_PLAIN)
    @Path(value = "/isAlive")
    @GET
    open fun isAlive(): String {
        return "{status : \"ok\", message: \"Appen fungerer\"}"
    }

    @GET
    @Path(value = "/selftest")
    @Produces(MediaType.APPLICATION_JSON)
    open fun getSelftest(): SelftestResult {
        return selftestService.getSelftest()
    }
}

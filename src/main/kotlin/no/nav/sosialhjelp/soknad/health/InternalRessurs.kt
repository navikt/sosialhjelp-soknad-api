package no.nav.sosialhjelp.soknad.health

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.common.filedetection.MimeTypes
import no.nav.sosialhjelp.soknad.web.selftest.SelftestService
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestHtmlGenerator
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestJsonGenerator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import java.net.InetAddress
import java.net.UnknownHostException
import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

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
    @Produces(MediaType.TEXT_HTML)
    open fun getSelftest(@HeaderParam(value = HttpHeaders.ACCEPT) accept: String?): Response {
        val selftest = selftestService.lagSelftest()
        val response = Response.ok()
        if (MimeTypes.APPLICATION_JSON.equals(accept, ignoreCase = true)) {
            response.type(MediaType.APPLICATION_JSON).entity(SelftestJsonGenerator.generate(selftest)).build()
        } else {
            response.type(MediaType.TEXT_HTML).entity(SelftestHtmlGenerator.generate(selftest, host)).build()
        }
        return response.build()
    }

    private val host: String
        get() {
            var host = "unknown host"
            try {
                host = InetAddress.getLocalHost().canonicalHostName
            } catch (e: UnknownHostException) {
                log.error("Error retrieving host", e)
            }
            return host
        }

    companion object {
        private val log = LoggerFactory.getLogger(InternalRessurs::class.java)
    }
}

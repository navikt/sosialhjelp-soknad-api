package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.security.token.support.core.api.Unprotected;
import no.nav.sosialhjelp.soknad.web.selftest.SelftestService;
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestHtmlGenerator;
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestJsonGenerator;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static no.nav.sosialhjelp.soknad.business.util.MimeTypes.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.ACCEPT;

@Controller
@Unprotected
@Path("/internal")
public class InternalRessurs {

    private static final Logger log = getLogger(InternalRessurs.class);

    private final SelftestService selftestService;

    public InternalRessurs(SelftestService selftestService) {
        this.selftestService = selftestService;
    }

    @GET
    @Path(value = "/isAlive")
    @Produces({ MediaType.TEXT_PLAIN })
    public String isAlive() {
        return "{status : \"ok\", message: \"Appen fungerer\"}";
    }

    @GET
    @Path(value = "/selftest")
    @Produces({MediaType.TEXT_HTML})
    public Response getSelftest(@HeaderParam(value = ACCEPT) String accept) throws IOException {
        var selftest = selftestService.lagSelftest();
        var response = Response.ok();
        if (APPLICATION_JSON.equalsIgnoreCase(accept)) {
            response.type(MediaType.APPLICATION_JSON).entity(SelftestJsonGenerator.generate(selftest)).build();
        } else {
            response.type(MediaType.TEXT_HTML).entity(SelftestHtmlGenerator.generate(selftest, getHost())).build();
        }
        return response.build();
    }

    private String getHost() {
        var host = "unknown host";
        try {
            host = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.error("Error retrieving host", e);
        }
        return host;
    }

}

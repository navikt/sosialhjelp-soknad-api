package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Controller
@Unprotected
@Path("/internal")
public class InternalRessurs {

    @GET
    @Path(value = "/isAlive")
    @Produces({ MediaType.TEXT_PLAIN })
    public String isAlive() {

        return "{status : \"ok\", message: \"Appen fungerer\"}";
    }

}

package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.security.oidc.api.Unprotected;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.stream.Collectors;

@Controller
@Unprotected
@Path("/internal")
public class InternalRessurs {

    private static final Logger LOG = LoggerFactory.getLogger(InternalRessurs.class);

    @GET
    @Path(value = "/version")
    @Produces({ MediaType.TEXT_PLAIN })
    public String version() throws IOException {
        logAccess("version");
        StringBuilder ret = new StringBuilder();
        ret.append( IOUtils.toString(this.getClass().getResourceAsStream("/version.txt"))).append("\n");
        ret.append("java.version").append("=");
        ret.append(System.getProperty("java.version")).append("-");
        ret.append(System.getProperty("java.vendor")).append("\n\n");
        for (String s1 : System.getProperties().stringPropertyNames().stream().filter(s -> s.contains("withmock")).collect(Collectors.toList())) {
            ret.append(s1).append("=").append(System.getProperty(s1)).append("\n");
        }

        return ret.toString();
    }

    private void logAccess(String metode) {
        LOG.warn("InternalRessurs metode {} ble aksessert", metode);
    }
}

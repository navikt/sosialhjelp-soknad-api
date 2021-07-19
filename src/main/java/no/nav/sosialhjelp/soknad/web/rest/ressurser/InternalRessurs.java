package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.security.token.support.core.api.Unprotected;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import no.nav.sosialhjelp.soknad.web.selftest.SelftestService;
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestHtmlGenerator;
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestJsonGenerator;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
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
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.ACCEPT;

@Controller
@Unprotected
@Path("/internal")
public class InternalRessurs {

    private static final Logger log = getLogger(InternalRessurs.class);

    private final ApplicationContext appContext;
    private final SelftestService selftestService;

    protected List<Pingable.Ping> result;
    private volatile long lastResultTime;

    public InternalRessurs(ApplicationContext appContext, SelftestService selftestService) {
        this.appContext = appContext;
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
        doPing();
        var selftest = selftestService.lagSelftest();
        var response = Response.ok();
        if ("application/json".equalsIgnoreCase(accept)) {
            response.type(MediaType.APPLICATION_JSON).entity(SelftestJsonGenerator.generate(selftest)).build();
        } else {
            response.type(MediaType.TEXT_HTML).entity(SelftestHtmlGenerator.generate(selftest, getHost())).build();
        }
        return response.build();
    }

    protected void doPing() {
        long requestTime = System.currentTimeMillis();
        // Beskytter pingables mot mange samtidige/tette requester.
        // Særlig viktig hvis det tar lang tid å utføre alle pingables
        synchronized (this) {
            if (requestTime > lastResultTime) {
                result = getPingables().stream().map(PING).collect(toList());
                lastResultTime = System.currentTimeMillis();
            }
        }
    }

    protected Collection<Pingable> getPingables() {
        return appContext.getBeansOfType(Pingable.class).values();
    }

    protected String getHost() {
        var host = "unknown host";
        try {
            host = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.error("Error retrieving host", e);
        }
        return host;
    }

    private static final Function<Pingable, Pingable.Ping> PING = pingable -> {
        long startTime = System.currentTimeMillis();
        var ping = pingable.ping();
        ping.setResponstid(System.currentTimeMillis() - startTime);
        if (!ping.erVellykket()) {
            log.warn("Feil ved SelfTest av {}", ping.getMetadata().getEndepunkt(), ping.getFeil());
        }
        return ping;
    };

}

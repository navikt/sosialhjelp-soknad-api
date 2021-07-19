package no.nav.sosialhjelp.soknad.web.rest.ressurser;

import no.nav.security.token.support.core.api.Unprotected;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import no.nav.sosialhjelp.soknad.web.selftest.domain.Selftest;
import no.nav.sosialhjelp.soknad.web.selftest.domain.SelftestEndpoint;
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestHtmlGenerator;
import no.nav.sosialhjelp.soknad.web.selftest.generators.SelftestJsonGenerator;
import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.ACCEPT;

@Controller
@Unprotected
@Path("/internal")
public class InternalRessurs {

    private static final Logger log = getLogger(InternalRessurs.class);

    @Inject
    private ApplicationContext appContext;

    protected List<Pingable.Ping> result;
    private volatile long lastResultTime;

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
        var selftest = lagSelftest();
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

    protected Integer getAggregertStatus() {
        boolean harKritiskFeil = result.stream().anyMatch(KRITISK_FEIL);
        boolean harFeil = result.stream().anyMatch(HAR_FEIL);

        if (harKritiskFeil) {
            return STATUS_ERROR;
        } else if (harFeil) {
            return STATUS_WARNING;
        }
        return STATUS_OK;
    }

    protected String getHost() {
        String host = "unknown host";
        try {
            host = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.error("Error retrieving host", e);
        }
        return host;
    }

    private static final Function<Pingable, Pingable.Ping> PING = pingable -> {
        long startTime = System.currentTimeMillis();
        Pingable.Ping ping = pingable.ping();
        ping.setResponstid(System.currentTimeMillis() - startTime);
        if (!ping.erVellykket()) {
            log.warn("Feil ved SelfTest av {}", ping.getMetadata().getEndepunkt(), ping.getFeil());
        }
        return ping;
    };

    private Selftest lagSelftest() {
        return new Selftest()
                .setApplication(MiljoUtils.getNaisAppName())
                .setVersion(MiljoUtils.getNaisAppImage())
                .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .setAggregateResult(getAggregertStatus())
                .setChecks(result.stream()
                        .map(InternalRessurs::lagSelftestEndpoint)
                        .collect(toList())
                );
    }

    private static SelftestEndpoint lagSelftestEndpoint(Pingable.Ping ping) {
        return new SelftestEndpoint()
                .setEndpoint(ping.getMetadata().getEndepunkt())
                .setDescription(ping.getMetadata().getBeskrivelse())
                .setErrorMessage(ping.getFeilmelding())
                .setCritical(ping.getMetadata().isKritisk())
                .setResult(ping.harFeil() ? STATUS_ERROR : STATUS_OK)
                .setResponseTime(String.format("%dms", ping.getResponstid()))
                .setStacktrace(ofNullable(ping.getFeil())
                        .map(ExceptionUtils::getStackTrace)
                        .orElse(null)
                );
    }

    private static final Predicate<Pingable.Ping> KRITISK_FEIL = ping -> ping.harFeil() && ping.getMetadata().isKritisk();
    private static final Predicate<Pingable.Ping> HAR_FEIL = Pingable.Ping::harFeil;

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = 1;
    public static final int STATUS_WARNING = 2;

}

package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.selftest.SelfTest;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.config.ContentConfig;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Controller
@Path("/internal")
public class InternalRessurs {
    @Inject
    private ContentConfig contentConfig;
    @Inject
    private LagringsScheduler lagringsScheduler;
    @Inject
    private CacheManager cacheManager;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private SelfTest selfTest;

    @GET
    @Path("/selftest")
    @Produces(MediaType.TEXT_HTML)
    public String selftest(@Context HttpServletRequest request) {
        return selfTest.asHtml(request.getServletContext());
    }

    @GET
    @Path("/selftest.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> selftestJSON(@Context HttpServletRequest request) {
        return selfTest.asJson(request.getServletContext());
    }

    @GET
    @Path("/isAlive")
    @Produces(MediaType.APPLICATION_JSON)
    public String isAlive() {
        return "{status: \"ok\", message: \"soknadsapiet fungerer\"}";
    }

    @POST
    @Path(value = "/lagre")
    public void kjorLagring() throws InterruptedException {
        lagringsScheduler.mellomlagreSoknaderOgNullstillLokalDb();
    }

    @GET
    @Path(value = "/resetcache")
    public String resetCache(@QueryParam("type") String type) {
        if ("cms".equals(type)) {
            cacheManager.getCache("cms.content").clear();
            cacheManager.getCache("cms.article").clear();
            contentConfig.lastInnNyeInnholdstekster();
            messageSource.clearCache();
            return "CACHE RESET OK";
        }
        return "OK";
    }

}

package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;


@Controller
@Path("/internal")
public class InternalRessurs {
    @Inject
    private LagringsScheduler lagringsScheduler;
    @Inject
    private CacheManager cacheManager;
    @Inject
    private NavMessageSource messageSource;

    private static final Logger LOG = LoggerFactory.getLogger(InternalRessurs.class);

    @POST
    @Path(value = "/lagre")
    public void kjorLagring() throws InterruptedException {
        logAccess("kjorLagring");
        lagringsScheduler.mellomlagreSoknaderOgNullstillLokalDb();
    }

    @GET
    @Path(value = "/resetcache")
    public String resetCache(@QueryParam("type") String type) {
        logAccess("resetCache");
        if ("cms".equals(type)) {
            cacheManager.getCache("cms.content").clear();
            cacheManager.getCache("cms.article").clear();
            messageSource.clearCache();
            return "CACHE RESET OK";
        }
        return "OK";
    }

    private void logAccess(String metode) {
        LOG.warn("InternalRessurs metode {} ble aksessert", metode);
    }
}

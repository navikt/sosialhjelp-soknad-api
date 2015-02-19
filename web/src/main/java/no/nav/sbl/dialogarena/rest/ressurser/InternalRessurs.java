package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SelfTestService;
import no.nav.sbl.dialogarena.websoknad.config.ContentConfig;
import org.springframework.cache.CacheManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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


    @GET
    @Path("/selftest")
    @Produces(MediaType.TEXT_HTML)
    public String selftest() {
        SelfTestService service = new SelfTestService("Søknads api");
        return service.buildHtmlString();
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

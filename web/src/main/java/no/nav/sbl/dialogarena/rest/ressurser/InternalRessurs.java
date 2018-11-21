package no.nav.sbl.dialogarena.rest.ressurser;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.URLTemplateSource;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.sbl.dialogarena.service.helpers.HvisLikHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;


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

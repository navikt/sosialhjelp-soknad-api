package no.nav.sbl.dialogarena.rest.ressurser;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.URLTemplateSource;
import no.nav.sbl.dialogarena.common.suspend.SuspendServlet;
import no.nav.sbl.dialogarena.config.ContentConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
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
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

import static no.nav.sbl.dialogarena.rest.utils.MocksetupUtils.*;

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

    private PersonPortTypeMock personPortTypeMock = PersonMock.getInstance().getPersonPortTypeMock();

    private static final Logger LOG = LoggerFactory.getLogger(InternalRessurs.class);

    @GET
    @Path("/isAlive")
    @Produces(MediaType.APPLICATION_JSON)
    public String isAlive(@Context HttpServletResponse response) throws IOException {
        if(!SuspendServlet.isRunning()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return "Service is suspended for application update";
        } else {
            return "{status: \"ok\", message: \"soknadsapiet fungerer\"}";
        }
    }

    @POST
    @Path(value = "/lagre")
    public void kjorLagring() throws InterruptedException {
        logAccess("kjorLagring");
        lagringsScheduler.mellomlagreSoknaderOgNullstillLokalDb();
    }

    @GET
    @Path(value = "/mocksetup")
    public String mocksetup(@Context ServletContext servletContext) throws IOException {
        logAccess("mocksetup");
        MocksetupFields fields = getMocksetupFields();

        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper(HvisLikHelper.NAVN, new HvisLikHelper());
        Template compile = handlebars.compile(new URLTemplateSource("mocksetup.hbs", servletContext.getResource("/WEB-INF/mocksetup.hbs")));
        com.github.jknack.handlebars.Context context = com.github.jknack.handlebars.Context
                .newBuilder(fields)
                .resolver(MethodValueResolver.INSTANCE)
                .build();
        return compile.apply(context);
    }

    @POST
    @Path(value = "/mocksetup")
    public Response mocksetup(@FormParam("statsborgerskap") String statsborgerskap,
                                  @FormParam("kode6") String kode6,
                                  @FormParam("primar_adressetype") String primarAdressetype,
                                  @FormParam("sekundar_adressetype") String sekundarAdressetype) throws InterruptedException {
        logAccess("mocksetupPost");
        Boolean skalHaKode6 = "true".equalsIgnoreCase(kode6);

        Person person = personPortTypeMock.getPerson();
        person.setDiskresjonskode(skalHaKode6 ? getDiskresjonskode() : null);
        person.getStatsborgerskap().getLand().setValue(statsborgerskap.toUpperCase());
        settPostadressetype(primarAdressetype);
        settSekundarAdressetype(sekundarAdressetype);

        return Response.seeOther(URI.create("/sendsoknad/internal/mocksetup")).build();
    }


    @GET
    @Path(value = "/resetcache")
    public String resetCache(@QueryParam("type") String type) {
        logAccess("resetCache");
        if ("cms".equals(type)) {
            cacheManager.getCache("cms.content").clear();
            cacheManager.getCache("cms.article").clear();
            contentConfig.lastInnNyeInnholdstekster();
            messageSource.clearCache();
            return "CACHE RESET OK";
        }
        return "OK";
    }

    private void logAccess(String metode) {
        LOG.warn("InternalRessurs metode {} ble aksessert", metode);
    }
}

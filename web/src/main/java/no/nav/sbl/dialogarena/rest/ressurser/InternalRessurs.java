package no.nav.sbl.dialogarena.rest.ressurser;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.URLTemplateSource;
import no.nav.sbl.dialogarena.config.ContentConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.FunksjonalitetBryter;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

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
    @Context
    private ServletContext servletContext;


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
    @Path(value = "/funksjon")
    public String endreFunksjonalitet() throws Exception {
        Handlebars handlebars = new Handlebars();
        Template compile = handlebars.compile(new URLTemplateSource("funksjonalitetsBryter.html", servletContext.getResource("/WEB-INF/funksjonalitetsBryter.html")));
        com.github.jknack.handlebars.Context context = com.github.jknack.handlebars.Context
                .newBuilder(FunksjonalitetBryter.values())
                .resolver(FieldValueResolver.INSTANCE, MethodValueResolver.INSTANCE)
                .build();
        return compile.apply(context);
    }
    @POST
    @Path(value = "/funksjon")
    public Response endreFunksjonalitetBryter(@FormParam("bryternavn") FunksjonalitetBryter bryter, @FormParam("status") String status) throws InterruptedException {
        System.out.println("setter " + bryter + " til " + status);
        System.setProperty(bryter.nokkel, "on".equals(status)? "true": "false");
        return Response.seeOther(URI.create("/sendsoknad/internal/funksjon")).build();
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

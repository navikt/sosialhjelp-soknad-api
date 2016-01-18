package no.nav.sbl.dialogarena.rest.ressurser;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.URLTemplateSource;
import no.nav.sbl.dialogarena.config.ContentConfig;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.FunksjonalitetBryter;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static no.nav.sbl.dialogarena.rest.utils.MockdataUtils.*;

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
    private SoknadService soknadService;
    @Inject
    private VedleggService vedleggService;
    @Inject
    private HtmlGenerator pdfTemplate;
    @Context
    private ServletContext servletContext;

    private PersonPortTypeMock personPortTypeMock;

    InternalRessurs(){
        personPortTypeMock = PersonMock.getInstance().personMock();
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
    @Path(value = "/funksjon")
    public String endreFunksjonalitet() throws IOException {
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
    public Response endreFunksjonalitetBryter(@FormParam("bryternavn") List<String> brytere, @FormParam("status") List<String> status) throws InterruptedException {

        for (String bryter : brytere) {
            int index = Character.getNumericValue(bryter.charAt(0));
            FunksjonalitetBryter bryterUtenIndeks = FunksjonalitetBryter.valueOf(brytere.get(index).substring(1));
            String nyStatus = status.contains(index + "true") ? "true" : "false";
            System.setProperty(bryterUtenIndeks.nokkel, nyStatus);
        }

        return Response.seeOther(URI.create("/sendsoknad/internal/funksjon")).build();
    }

    @GET
    @Path(value = "/mockdata")
    public String endreMockdata() throws IOException {
        MockdataFields fields = getMockdataFields(personPortTypeMock);

        Handlebars handlebars = new Handlebars();
        Template compile = handlebars.compile(new URLTemplateSource("endreMockData.hbs", servletContext.getResource("/WEB-INF/endreMockData.hbs")));
        com.github.jknack.handlebars.Context context = com.github.jknack.handlebars.Context
                .newBuilder(fields)
                .resolver(MethodValueResolver.INSTANCE)
                .build();
        return compile.apply(context);
    }

    @POST
    @Path(value = "/mockdata")
    public Response endreMockData(@FormParam("utenlandskstatsborger") String utenlandskStatsborger,
                                  @FormParam("kode6") String kode6,
                                  @FormParam("submit") String submit) throws InterruptedException {
        Boolean skalHaKode6 = "true".equalsIgnoreCase(kode6);
        Boolean erUtenlandskStatsborger = "true".equalsIgnoreCase(utenlandskStatsborger);

        Person person = personPortTypeMock.getPerson();
        person.setDiskresjonskode(skalHaKode6 ? getDiskresjonskode() : null);
        setLandPaaStatsborgerskap(person.getStatsborgerskap(), erUtenlandskStatsborger ? "GER" : "NOR");
        return Response.seeOther(URI.create("/sendsoknad/internal/mockdata")).build();
    }

    @GET
    @Path("/{behandlingsId}/nyoppsummering")
    @Produces(TEXT_HTML)
    public String hentOppsummeringNew(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        return pdfTemplate.fyllHtmlMalMedInnhold(soknad);
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

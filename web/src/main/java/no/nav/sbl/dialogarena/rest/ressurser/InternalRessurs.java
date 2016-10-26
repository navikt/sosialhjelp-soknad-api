package no.nav.sbl.dialogarena.rest.ressurser;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.URLTemplateSource;
import no.nav.sbl.dialogarena.config.ContentConfig;
import no.nav.sbl.dialogarena.rest.utils.PDFService;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.service.helpers.HvisLikHelper;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.FunksjonalitetBryter;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
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
    @Inject
    private SoknadDataFletter soknadDataFletter;
    @Inject
    private VedleggService vedleggService;
    @Inject
    private HtmlGenerator pdfTemplate;
    @Inject
    private PDFService pdfService;


    private PersonPortTypeMock personPortTypeMock = PersonMock.getInstance().getPersonPortTypeMock();


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
    public String endreFunksjonalitet(@Context ServletContext servletContext) throws IOException {
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
    @Path(value = "/mocksetup")
    public String mocksetup(@Context ServletContext servletContext) throws IOException {
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
        if ("cms".equals(type)) {
            cacheManager.getCache("cms.content").clear();
            cacheManager.getCache("cms.article").clear();
            contentConfig.lastInnNyeInnholdstekster();
            messageSource.clearCache();
            return "CACHE RESET OK";
        }
        return "OK";
    }

    @GET
    @Path("/{behandlingsId}/nyoppsummering")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String hentOppsummeringNew(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        return pdfTemplate.fyllHtmlMalMedInnhold(soknad);
    }

    @GET
    @Path("/{behandlingsId}/fullsoknad")
    @Produces(TEXT_HTML)
    @SjekkTilgangTilSoknad
    public String fullSoknad(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        return pdfTemplate.fyllHtmlMalMedInnhold(soknad, true);
    }
    @GET
    @Path("/{behandlingsId}/fullsoknadpdf")
    @Produces("application/pdf")
    @SjekkTilgangTilSoknad
    public byte[] fullSoknadPdf(@PathParam("behandlingsId") String behandlingsId, @Context ServletContext servletContext) throws IOException {
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());
        String servletPath = servletContext.getRealPath("/");
        return pdfService.genererOppsummeringPdf(soknad, servletPath, true);
    }
}

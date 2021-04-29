package no.nav.sosialhjelp.soknad.web.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.svarut.servicesv9.Dokument;
import no.ks.svarut.servicesv9.Forsendelse;
import no.ks.svarut.servicesv9.PostAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksSender;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.consumer.bostotte.MockBostotteImpl;
import no.nav.sosialhjelp.soknad.consumer.dkif.DkifConsumerMock;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkConsumerMock;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonConsumerMock;
import no.nav.sosialhjelp.soknad.consumer.pdl.PdlConsumerMock;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumerMock;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.mock.adresse.AdresseSokConsumerMock;
import no.nav.sosialhjelp.soknad.oppslag.OppslagConsumerMock;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils.isTillatMockRessurs;

@Controller
@Unprotected
@Path("/internal/mock/tjeneste")
@Produces(APPLICATION_JSON)
public class TjenesteMockRessurs {

    private static final Logger logger = LoggerFactory.getLogger(TjenesteMockRessurs.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Inject
    private CacheManager cacheManager;
    @Inject
    private InnsendingService innsendingService;
    @Inject
    private FiksSender fiksSender;

    private void clearCache() {
        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    @GET
    @Path("/pdfboxtest")
    public void generatePdfAndSaveToDisk() {

        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        SosialhjelpPdfGenerator sosialhjelpPdfGenerator = new SosialhjelpPdfGenerator();

        byte[] bytes = sosialhjelpPdfGenerator.generate(new JsonInternalSoknad().withSoknad(
                new JsonSoknad().withData(new JsonData().withPersonalia(new JsonPersonalia().withNavn(
                        new JsonSokernavn().withFornavn("Han").withEtternavn("Solo")
                        ))
                )), false);

        try {
            FileOutputStream out = new FileOutputStream("starcraft.pdf");
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/uid")
    public void setUid(@RequestBody MockUid uid) {
        logger.info("setUid {}", uid.getUid());
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        session.setAttribute("mockRessursUid", uid.getUid());
        clearCache();
    }

    private byte[] createZipByteArray(List<Dokument> dokumenter) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (Dokument dokument : dokumenter) {
                ZipEntry zipEntry = new ZipEntry(dokument.getFilnavn());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(IOUtils.toByteArray(dokument.getData().getInputStream()));
                zipOutputStream.closeEntry();
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    @GET
    @Consumes(APPLICATION_JSON)
    @Path("/downloadzip/{behandlingsId}")
    public Response downloadZip(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletResponse response) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        String eier = SubjectHandler.getUserId();
        SendtSoknad sendtSoknad = innsendingService.hentSendtSoknad(behandlingsId, eier);
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(sendtSoknad.getNavEnhetsnavn())
                .withPostnr("0000")
                .withPoststed("Ikke send");

        Forsendelse forsendelse = fiksSender.opprettForsendelse(sendtSoknad, fakeAdresse);

        try {
            byte[] zipByteArray = createZipByteArray(forsendelse.getDokumenter());
            return Response
                    .ok(zipByteArray)
                    .type("application/zip")
                    .header("Content-Disposition", "attachment; filename=\"fiks_forsendelse_" + eier + ".zip\"")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        clearCache();
        return Response.noContent().build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/adresser")
    public void setAdresser(@RequestBody String jsonAdressesokRespons) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        AdresseSokConsumerMock.setAdresser(jsonAdressesokRespons);
        clearCache();
    }

    class SessionResponse {
        public String uid;

        SessionResponse(String uid) {
            this.uid = uid;
        }
    }

    @GET
    @Path("/session")
    public Response getSession() {
        return Response.ok(new SessionResponse(SubjectHandler.getUserId())).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/telefon")
    public void setTelefon(@RequestBody JsonTelefonnummer jsonTelefonnummer, @QueryParam("fnr") String fnr) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        fnr = SubjectHandler.getUserId() != null ? SubjectHandler.getUserId() : fnr;
        logger.warn("Setter telefonnummer for bruker. Dette skal aldri skje i PROD.");
        if (jsonTelefonnummer != null) {
            DkifConsumerMock.setTelefonnummer(jsonTelefonnummer.getVerdi(), fnr);
        } else {
            DkifConsumerMock.resetTelefonnummer(fnr);
        }
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/brukerprofil")
    public void setBrukerprofil(@RequestBody String jsonBrukerProfil) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        OppslagConsumerMock.setKontonummer(jsonBrukerProfil);
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/arbeid")
    public void setArbeidsforholdJson(@RequestBody String arbeidsforholdData) throws JsonProcessingException {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        // do nothing
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/organisasjon")
    public void setOrganisasjon(@RequestBody String jsonOrganisasjon) throws JsonProcessingException {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        logger.info("Setter mock organisasjon med data: {}", jsonOrganisasjon);
        if (jsonOrganisasjon != null && mapper.readTree(jsonOrganisasjon).has("organisasjon")) {
            String strippedOrganisasjon = mapper.readTree(jsonOrganisasjon).get("organisasjon").toString();
            OrganisasjonConsumerMock.setOrganisasjon(strippedOrganisasjon);
        } else {
            OrganisasjonConsumerMock.resetOrganisasjon();
        }
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/familie")
    public void setFamilie(@RequestBody String jsonPerson) throws JsonProcessingException {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.info("Setter mock familieforhold med data: {}", jsonPerson);

        var mockFamiliedata = mapper.readValue(jsonPerson, PdlConsumerMock.PdlMockResponse.class);
        if (mockFamiliedata.getPerson().getStatsborgerskap() != null) {
            KodeverkConsumerMock.leggTilLandkode(mockFamiliedata.getPerson().getStatsborgerskap());
        }

        PdlConsumerMock.setPerson(mockFamiliedata);
        if (mockFamiliedata.getEktefelle().getIdent() != null){
            PdlConsumerMock.setEktefelle(mockFamiliedata.getEktefelle());
        }
        mockFamiliedata.getBarn().forEach(PdlConsumerMock::setBarn);

        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/utbetaling")
    public void setUtbetalinger(@RequestBody String jsonWSUtbetaling) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        // setter kun default-utbetaling her.
        OppslagConsumerMock.setUtbetalinger();
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/utbetaling_feiler")
    public void setNavUtbetalingerFeiler(@RequestBody boolean skalFeile, @QueryParam("fnr") String fnr) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        // do nothing
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/skattetaten")
    public void setSkattUtbetalinger(@RequestBody String jsonWSSkattUtbetaling, @QueryParam("fnr") String fnr) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        fnr = SubjectHandler.getUserId() != null ? SubjectHandler.getUserId() : fnr;
        SkattbarInntektConsumerMock.setMockData(fnr, jsonWSSkattUtbetaling);
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/skattetaten_feiler")
    public void setSkattUtbetalingerFeiler(@RequestBody boolean skalFeile, @QueryParam("fnr") String fnr) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        fnr = SubjectHandler.getUserId() != null ? SubjectHandler.getUserId() : fnr;
        SkattbarInntektConsumerMock.setMockSkalFeile(fnr, skalFeile);
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/norg")
    public void setNorg(@RequestBody String rsNorgEnhetMap) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        // do nothing
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/bostotte")
    public void setBostotte(@RequestBody String bostotteJson, @QueryParam("fnr") String fnr) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        fnr = SubjectHandler.getUserId() != null ? SubjectHandler.getUserId() : fnr;
        MockBostotteImpl.setBostotteData(fnr, bostotteJson);
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/bostotte_feiler")
    public void setBostotte(@RequestBody Boolean skalFeile, @QueryParam("fnr") String fnr) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        if(skalFeile != null) {
            fnr = SubjectHandler.getUserId() != null ? SubjectHandler.getUserId() : fnr;
            MockBostotteImpl.settPersonnummerSomSkalFeile(fnr, skalFeile);
        }
    }
}

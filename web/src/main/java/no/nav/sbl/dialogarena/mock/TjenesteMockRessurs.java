package no.nav.sbl.dialogarena.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.ks.svarut.servicesv9.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg.NorgConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon.OrganisasjonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
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
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller
@Path("/internal/mock/tjeneste")
@Produces(APPLICATION_JSON)
public class TjenesteMockRessurs {

    private static final Logger logger = LoggerFactory.getLogger(TjenesteMockRessurs.class);

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

    public static boolean isTillatMockRessurs() {
        return Boolean.parseBoolean(System.getProperty("tillatMockRessurs", "false"));
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/uid")
    public void setUid(@RequestBody String uid) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            MockUid mockUid = mapper.readValue(uid, MockUid.class);
            final ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            final HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("mockRessursUid", mockUid.getUid());
        } catch (IOException e) {
            e.printStackTrace();
        }
        clearCache();
    }


    public byte[] createZipByteArray(List<Dokument> dokumenter) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        try {
            for (Dokument dokument : dokumenter) {
                ZipEntry zipEntry = new ZipEntry(dokument.getFilnavn());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(IOUtils.toByteArray(dokument.getData().getInputStream()));
                zipOutputStream.closeEntry();
            }
        } finally {
            zipOutputStream.close();
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

        String eier = SubjectHandler.getUserIdFromToken();
        final SendtSoknad sendtSoknad = innsendingService.hentSendtSoknad(behandlingsId, eier);
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


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/telefon")
    public void setTelefon(@RequestBody JsonTelefonnummer jsonTelefonnummer) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.warn("Setter telefonnummer: " + jsonTelefonnummer.getVerdi() + ". For bruker med uid: " + SubjectHandler.getUserIdFromToken());
        if (jsonTelefonnummer != null){
            DkifMock.setTelefonnummer(jsonTelefonnummer);
        } else {
            DkifMock.resetTelefonnummer();
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
        BrukerprofilMock.setBrukerprofil(jsonBrukerProfil);
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/arbeid")
    public void setArbeidsforholdJson(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.info("Setter arbeidsforhold: " + arbeidsforholdData);
        ArbeidsforholdMock.setArbeidsforhold(arbeidsforholdData);
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_XML)
    @Path("/arbeid")
    public void setArbeidsforholdXml(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        ArbeidsforholdMock.setArbeidsforhold(arbeidsforholdData);
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/organisasjon")
    public void setOrganisasjon(@RequestBody String jsonOrganisasjon) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.info("Setter mock organisasjon med data: " + jsonOrganisasjon);
        if (jsonOrganisasjon != null){
            OrganisasjonMock.setOrganisasjon(jsonOrganisasjon);
        } else {
            OrganisasjonMock.resetOrganisasjon();
        }
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/familie")
    public void setFamilie(@RequestBody String jsonPerson) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.info("Setter mock familieforhold med data: " + jsonPerson);
        PersonMock.setPersonMedFamilieforhold(jsonPerson);
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/utbetaling")
    public void setUtbetalinger(@RequestBody String jsonWSUtbetaling) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        UtbetalMock.setUtbetalinger(jsonWSUtbetaling);
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/norg")
    public void setNorg(@RequestBody String rsNorgEnhetMap) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        NorgConsumerMock.setNorgMap(rsNorgEnhetMap);
        clearCache();
    }
}

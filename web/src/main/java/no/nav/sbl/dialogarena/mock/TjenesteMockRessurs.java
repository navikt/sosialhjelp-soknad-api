package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg.NorgConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon.OrganisasjonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksSender;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.AlternativRepresentasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.sosialhjelp.InnsendingService;
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
    @Inject
    private AlternativRepresentasjonService alternativRepresentasjonService;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private SoknadDataFletter soknadDataFletter;


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
    public void setUid(@RequestBody MockUid uid) {
        logger.info("setUid " + uid.getUid());
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        final ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final HttpSession session = attr.getRequest().getSession(true);
        session.setAttribute("mockRessursUid", uid.getUid());
        clearCache();
    }

    @GET
    @Path("/json/{behandlingsId}")
    @Produces(APPLICATION_JSON)
    public byte[] jsonRepresentasjon(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true, false);
        List<AlternativRepresentasjon> representasjoner = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad, messageSource);
        return null; // FIXME: getJsonRepresentasjon(representasjoner, behandlingsId, soknad);
    }

    /* FIXME: Drar inn transitive avhengigheter som må deklareres i pom
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
    }*/

    @GET
    @Consumes(APPLICATION_JSON)
    @Path("/downloadzip/{behandlingsId}")
    public Response downloadZip(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletResponse response) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        String eier = getUid();
        /* FIXME:
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

        clearCache();*/
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
        return Response.ok(new SessionResponse(getUid())).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/telefon")
    public void setTelefon(@RequestBody JsonTelefonnummer jsonTelefonnummer, @QueryParam("fnr") String fnr ) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        fnr = getUid() != null ? getUid() : fnr;
        logger.warn("Setter telefonnummer: " + jsonTelefonnummer.getVerdi() + ". For bruker med fnr: " + fnr);
        if (jsonTelefonnummer != null){
            DkifMock.setTelefonnummer(jsonTelefonnummer, fnr);
        } else {
            DkifMock.resetTelefonnummer(fnr);
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

    private String getUid() {
        if (!TjenesteMockRessurs.isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            String uid = (String) session.getAttribute("mockRessursUid");
            logger.info("getUid " + uid);
            return uid;
        } catch (RuntimeException e) {
            return null;
        }
    }
}

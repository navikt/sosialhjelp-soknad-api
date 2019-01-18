package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg.NorgConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon.OrganisasjonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Controller
@Path("/internal/mock/tjeneste")
@Produces(APPLICATION_JSON)
public class TjenesteMockRessurs {
    private static final Logger logger = LoggerFactory.getLogger(TjenesteMockRessurs.class);


    @Inject
    private CacheManager cacheManager;


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

        final ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final HttpSession session = attr.getRequest().getSession(true);
        session.setAttribute("mockRessursUid", uid);

        clearCache();
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

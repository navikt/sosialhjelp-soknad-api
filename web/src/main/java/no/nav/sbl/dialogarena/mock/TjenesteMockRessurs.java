package no.nav.sbl.dialogarena.mock;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler;
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

import javax.inject.Inject;
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
    @Path("/adresser")
    public void setAdresser(@RequestBody String jsonAdressesokRespons) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        AdresseSokConsumerMock.setAdresser(jsonAdressesokRespons);
        clearCache();
    }

    @DELETE
    @Path("/adresser")
    public void resetAdresser() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        AdresseSokConsumerMock.resetAdresser();
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/telefon")
    public void setTelefon(@RequestBody JsonTelefonnummer jsonTelefonnummer) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.warn("Setter telefonnummer: " + jsonTelefonnummer.getVerdi() + ". For bruker med uid: " + OidcSubjectHandler.getSubjectHandler().getUserIdFromToken());
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

    @DELETE
    @Path("/brukerprofil")
    public void setDefaultBrukerprofil() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        BrukerprofilMock.resetBrukerprofil();
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

    @DELETE
    @Path("/arbeid")
    public void resetAlleArbeidsforhold() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        ArbeidsforholdMock.resetArbeidsforhold();
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

    @DELETE
    @Path("/familie")
    public void resetFamilie() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        PersonMock.resetFamilieforhold();
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

    @DELETE
    @Path("/utbetaling")
    public void resetUtbetalinger() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        UtbetalMock.resetUtbetalinger();
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

    @DELETE
    @Path("/norg")
    public void resetNorg() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        NorgConsumerMock.resetNorgMap();
        clearCache();
    }

}

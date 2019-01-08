package no.nav.sbl.dialogarena.rest.ressurser.mock;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg.NorgConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon.OrganisasjonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.security.auth.Subject;
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

    private boolean isTillatMockRessurs() {
        return Boolean.parseBoolean(System.getProperty("tillatMockRessurs", "false"));
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/adresser")
    public void settAdresser(@RequestBody String jsonAdressesokRespons) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        AdresseSokConsumerMock.setAdresser(jsonAdressesokRespons);
        clearCache();
    }

    @DELETE
    @Path("/adresser")
    public void slettAdresser() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        AdresseSokConsumerMock.settDefaultAdresser();
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/telefon")
    public void settTelefon(@RequestBody JsonTelefonnummer jsonTelefonnummer) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.warn("Setter telefonnummer: " + jsonTelefonnummer.getVerdi() + ". For bruker med uid: " + SubjectHandler.getSubjectHandler().getUid());
        if (jsonTelefonnummer != null){
            DkifMock.setTelefonnummer(jsonTelefonnummer);
        } else {
            DkifMock.slettTelefonnummer();
        }
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/brukerprofil")
    public void settBrukerprofil(@RequestBody String jsonBrukerProfil) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        BrukerprofilMock.setBrukerprofil(jsonBrukerProfil);
        clearCache();
    }

    @DELETE
    @Path("/brukerprofil")
    public void settDefaultBrukerprofil() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        BrukerprofilMock.settDefaultBrukerprofil();
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/arbeid")
    public void settArbeidsforholdJson(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.info("Setter arbeidsforhold: " + arbeidsforholdData);
        ArbeidsforholdMock.settArbeidsforhold(arbeidsforholdData);
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_XML)
    @Path("/arbeid")
    public void settArbeidsforholdXml(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        ArbeidsforholdMock.settArbeidsforhold(arbeidsforholdData);
        clearCache();
    }

    @DELETE
    @Path("/arbeid")
    public void slettAlleArbeidsforhold() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        ArbeidsforholdMock.slettAlleArbeidsforhold();
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/organisasjon")
    public void settOrganisasjon(@RequestBody String jsonOrganisasjon) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.info("Setter mock organisasjon med data: " + jsonOrganisasjon);
        if (jsonOrganisasjon != null){
            OrganisasjonMock.setOrganisasjon(jsonOrganisasjon);
        } else {
            OrganisasjonMock.slettOrganisasjon();
        }
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/familie")
    public void settFamilie(@RequestBody String jsonPerson) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        logger.info("Setter mock familieforhold med data: " + jsonPerson);
        PersonMock.setPersonMedFamilieforhold(jsonPerson);
        clearCache();
    }

    @DELETE
    @Path("/familie")
    public void slettFamilie() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        PersonMock.setDefaultPersonUtenFamilieforhold();
        clearCache();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/utbetaling")
    public void settUtbetalinger(@RequestBody String jsonWSUtbetaling) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        UtbetalMock.setUtbetalinger(jsonWSUtbetaling);
        clearCache();
    }

    @DELETE
    @Path("/utbetaling")
    public void slettUtbetalinger() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        UtbetalMock.slettUtbetalinger();
        clearCache();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/norg")
    public void settNorg(@RequestBody String rsNorgEnhetMap) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        NorgConsumerMock.settNorgMap(rsNorgEnhetMap);
        clearCache();
    }

    @DELETE
    @Path("/norg")
    public void slettNorg() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        NorgConsumerMock.slettNorgMap();
        clearCache();
    }

}

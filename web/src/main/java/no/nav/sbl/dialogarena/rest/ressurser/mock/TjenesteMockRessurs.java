package no.nav.sbl.dialogarena.rest.ressurser.mock;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg.NorgConsumerMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon.OrganisasjonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
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
        clearCache();
        AdresseSokConsumerMock.setAdresser(jsonAdressesokRespons);
    }

    @DELETE
    @Path("/adresser")
    public void slettAdresser() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        AdresseSokConsumerMock.settDefaultAdresser();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/telefon")
    public void settTelefon(@RequestBody JsonTelefonnummer jsonTelefonnummer) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        DkifMock.setTelefonnummer(jsonTelefonnummer);
    }

    @DELETE
    @Path("/telefon")
    public void slettTelefon() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        DkifMock.slettTelefonnummer();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/kontonummer")
    public void settKontonummer(@RequestBody JsonKontonummer jsonKontonummer) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        BrukerprofilMock.setKontonummer(jsonKontonummer);
    }

    @DELETE
    @Path("/kontonummer")
    public void slettKontonummer() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        BrukerprofilMock.slettKontonummer();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/arbeid")
    public void settArbeidsforholdJson(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        ArbeidsforholdMock.settArbeidsforhold(arbeidsforholdData);
    }

    @POST
    @Consumes(APPLICATION_XML)
    @Path("/arbeid")
    public void settArbeidsforholdXml(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        ArbeidsforholdMock.settArbeidsforhold(arbeidsforholdData);
    }

    @DELETE
    @Path("/arbeid")
    public void slettAlleArbeidsforhold() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        ArbeidsforholdMock.slettAlleArbeidsforhold();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/organisasjon")
    public void settOrganisasjon(@RequestBody String jsonOrganisasjon) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        OrganisasjonMock.setOrganisasjon(jsonOrganisasjon);
    }

    @DELETE
    @Path("/organisasjon")
    public void slettOrganisasjon() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        OrganisasjonMock.slettOrganisasjon();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/familie")
    public void settFamilie(@RequestBody String jsonPerson) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        PersonMock.setPersonMedFamilieforhold(jsonPerson);
    }

    @DELETE
    @Path("/familie")
    public void slettFamilie() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        PersonMock.setDefaultPersonUtenFamilieforhold();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/utbetaling")
    public void settUtbetalinger(@RequestBody String jsonWSUtbetaling) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();

        UtbetalMock.setUtbetalinger(jsonWSUtbetaling);
    }

    @DELETE
    @Path("/utbetaling")
    public void slettUtbetalinger() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();

        UtbetalMock.slettUtbetalinger();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/norg")
    public void settNorg(@RequestBody String rsNorgEnhetMap) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();

        NorgConsumerMock.settNorgMap(rsNorgEnhetMap);
    }

    @DELETE
    @Path("/norg")
    public void slettNorg() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();

        NorgConsumerMock.slettNorgMap();
    }

}

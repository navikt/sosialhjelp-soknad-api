package no.nav.sbl.dialogarena.rest.ressurser.mock;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonArbeidConverter;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;

import java.util.ArrayList;
import java.util.List;

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
    @Path("/telefon/{telefon}")
    public void settTelefon(@PathParam("telefon") String telefon) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        DkifMock.setTelefonnummer(telefon);
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/kontonummer/{kontonummer}")
    public void settKontonummer(@PathParam("kontonummer") String kontonummer) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();
        BrukerprofilMock.setKontonummer(kontonummer);
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Path("/arbeid/forhold")
    public void nyttArbeidsforholdJson(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();

        ArbeidsforholdMock.leggTilArbeidsforhold(arbeidsforholdData);
    }

    @POST
    @Consumes(APPLICATION_XML)
    @Path("/arbeid/forhold")
    public void nyttArbeidsforholdXml(@RequestBody String arbeidsforholdData) {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }

        clearCache();

        ArbeidsforholdMock.leggTilArbeidsforhold(arbeidsforholdData);
    }

    @DELETE
    @Consumes(APPLICATION_JSON)
    @Path("/arbeid/forhold")
    public void slettAlleArbeidsforhold() {
        if (!isTillatMockRessurs()) {
            throw new RuntimeException("Mocking har ikke blitt aktivert.");
        }
        clearCache();

        ArbeidsforholdMock.slettAlleArbeidsforhold();
    }
}
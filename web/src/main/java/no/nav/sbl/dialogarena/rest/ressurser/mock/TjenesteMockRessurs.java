package no.nav.sbl.dialogarena.rest.ressurser.mock;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
        if (!telefon.isEmpty() && telefon.matches("^\\d{8}")) {
            DkifMock.setTelefonnummer(telefon);
        } else if (telefon.equals("slett")){
            DkifMock.setTelefonnummer(null);
        }
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
}
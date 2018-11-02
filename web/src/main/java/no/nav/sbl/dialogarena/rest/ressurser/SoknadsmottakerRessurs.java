package no.nav.sbl.dialogarena.rest.ressurser;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getFeaturesForEnhet;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;

@Controller
@Path("/soknadsmottaker")
@Timed
@Produces(APPLICATION_JSON)
public class SoknadsmottakerRessurs {
    private static final Logger logger = LoggerFactory.getLogger(SoknadsmottakerRessurs.class);

    @Inject
    private SoknadService soknadService;
    @Inject
    private NorgService norgService;
    @Inject
    private SoknadsmottakerService soknadsmottakerService;

    @GET
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public List<NavEnhetFrontend> hentSoknadsmottaker(@PathParam("behandlingsId") String behandlingsId, @QueryParam("valg") String valg, @Context HttpServletResponse response) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, false);

        final List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(webSoknad, valg);
        
        return adresseForslagene.stream().map((adresseForslag) -> {
            final NavEnhet navEnhet = norgService.finnEnhetForGt(adresseForslag.geografiskTilknytning);
            return mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslag, navEnhet);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    NavEnhetFrontend mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(AdresseForslag adresseForslag, NavEnhet navEnhet) {
        if (navEnhet == null) {
            logger.warn("Kunne ikke hente NAV-enhet: " + adresseForslag.geografiskTilknytning);
            return null;
        }
        if (adresseForslag.kommunenummer == null
                || adresseForslag.kommunenummer.length() != 4) {
            return null;
        }
        
        final boolean digisosKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(adresseForslag.kommunenummer);
        return new NavEnhetFrontend()
                .withEnhetsId(navEnhet.enhetNr)
                .withEnhetsnavn(navEnhet.navn)
                .withBydelsnummer(adresseForslag.bydel)
                .withKommunenummer(adresseForslag.kommunenummer)
                .withKommunenavn(adresseForslag.kommunenavn)
                .withSosialOrgnr((digisosKommune) ? navEnhet.sosialOrgnr : null)
                .withFeatures(getFeaturesForEnhet(navEnhet.enhetNr));
    }

    public static class NavEnhetFrontend {
        public String enhetsId;
        public String enhetsnavn;
        public String kommunenummer;
        public String kommunenavn;
        public String bydelsnummer;
        public String sosialOrgnr;
        public Map<String, Boolean> features;

        public NavEnhetFrontend() {

        }

        NavEnhetFrontend withEnhetsId(String enhetsId) {
            this.enhetsId = enhetsId;
            return this;
        }

        NavEnhetFrontend withEnhetsnavn(String enhetsnavn) {
            this.enhetsnavn = enhetsnavn;
            return this;
        }

        NavEnhetFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        NavEnhetFrontend withKommunenavn(String kommunenavn) {
            this.kommunenavn = kommunenavn;
            return this;
        }

        NavEnhetFrontend withBydelsnummer(String bydelsnummer) {
            this.bydelsnummer = bydelsnummer;
            return this;
        }

        NavEnhetFrontend withSosialOrgnr(String sosialOrgnr) {
            this.sosialOrgnr = sosialOrgnr;
            return this;
        }

        NavEnhetFrontend withFeatures(Map<String, Boolean> features) {
            this.features = features;
            return this;
        }
    }
}

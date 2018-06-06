package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getFeaturesForEnhet;

@Controller
@Path("/soknadsmottaker")
@Timed
@Produces(APPLICATION_JSON)
public class SoknadsmottakerRessurs {
    private static final Logger LOG = LoggerFactory.getLogger(SoknadsmottakerRessurs.class);

    @Inject
    private SoknadService soknadService;
    @Inject
    private NorgService norgService;
    @Inject
    private SoknadsmottakerService soknadsmottakerService;

    @GET
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public NavEnhetFrontend hentSoknadsmottaker(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletResponse response) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, false);

        final AdresseForslag adresseForslag = soknadsmottakerService.finnAdresseFraSoknad(webSoknad);
        if (adresseForslag == null) {
            return null;
        }
        final NavEnhet navEnhet = norgService.finnEnhetForGt(adresseForslag.geografiskTilknytning);

        return mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslag, navEnhet);
    }

    NavEnhetFrontend mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(AdresseForslag adresseForslag, NavEnhet navEnhet) {
        if (navEnhet == null) {
            LOG.warn("Kunne ikke hente NAV-enhet");
            return null;
        }
        return new NavEnhetFrontend()
                .withEnhetsId(navEnhet.enhetNr)
                .withEnhetsnavn(navEnhet.navn)
                .withBydelsnummer(adresseForslag.bydel)
                .withKommunenummer(adresseForslag.kommunenummer)
                .withKommunenavn(adresseForslag.kommunenavn)
                .withSosialOrgnr(navEnhet.sosialOrgnr)
                .withFeatures(getFeaturesForEnhet(navEnhet.enhetNr));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @SuppressWarnings("unused")
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

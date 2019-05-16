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

import no.nav.security.oidc.api.ProtectedWithClaims;
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
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
    public List<LegacyNavEnhetFrontend> hentSoknadsmottaker(@PathParam("behandlingsId") String behandlingsId, @QueryParam("valg") String valg, @Context HttpServletResponse response) {
        return findSoknadsmottaker(behandlingsId, valg);
    }

    public List<LegacyNavEnhetFrontend> findSoknadsmottaker(String behandlingsId, String valg){
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, false);

        final List<AdresseForslag> adresseForslagene = soknadsmottakerService.legacyFinnAdresseFraSoknad(webSoknad, valg);

        /*
         * Vi fjerner nå duplikate NAV-enheter med forskjellige bydelsnumre gjennom
         * bruk av distinct. Hvis det er viktig med riktig bydelsnummer bør dette kallet
         * fjernes og brukeren må besvare hvilken bydel han/hun oppholder seg i.
         */
        return adresseForslagene.stream().map((adresseForslag) -> {
            final NavEnhet navEnhet = norgService.finnEnhetForGt(adresseForslag.geografiskTilknytning);
            return mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslag, navEnhet);
        }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    LegacyNavEnhetFrontend mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(AdresseForslag adresseForslag, NavEnhet navEnhet) {
        if (navEnhet == null) {
            logger.warn("Kunne ikke hente NAV-enhet: " + adresseForslag.geografiskTilknytning);
            return null;
        }
        if (adresseForslag.kommunenummer == null
                || adresseForslag.kommunenummer.length() != 4) {
            return null;
        }
        
        final boolean digisosKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(adresseForslag.kommunenummer);
        return new LegacyNavEnhetFrontend()
                .withEnhetsId(navEnhet.enhetNr)
                .withEnhetsnavn(navEnhet.navn)
                .withBydelsnummer(adresseForslag.bydel)
                .withKommunenummer(adresseForslag.kommunenummer)
                .withKommunenavn(adresseForslag.kommunenavn)
                .withSosialOrgnr((digisosKommune) ? navEnhet.sosialOrgnr : null)
                .withFeatures(getFeaturesForEnhet(navEnhet.enhetNr));
    }

    public static class LegacyNavEnhetFrontend {
        public String enhetsId;
        public String enhetsnavn;
        public String kommunenummer;
        public String kommunenavn;
        public String bydelsnummer;
        public String sosialOrgnr;
        public Map<String, Boolean> features;

        public LegacyNavEnhetFrontend() {

        }

        public LegacyNavEnhetFrontend withEnhetsId(String enhetsId) {
            this.enhetsId = enhetsId;
            return this;
        }

        public LegacyNavEnhetFrontend withEnhetsnavn(String enhetsnavn) {
            this.enhetsnavn = enhetsnavn;
            return this;
        }

        public LegacyNavEnhetFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        public LegacyNavEnhetFrontend withKommunenavn(String kommunenavn) {
            this.kommunenavn = kommunenavn;
            return this;
        }

        public LegacyNavEnhetFrontend withBydelsnummer(String bydelsnummer) {
            this.bydelsnummer = bydelsnummer;
            return this;
        }

        public LegacyNavEnhetFrontend withSosialOrgnr(String sosialOrgnr) {
            this.sosialOrgnr = sosialOrgnr;
            return this;
        }

        public LegacyNavEnhetFrontend withFeatures(Map<String, Boolean> features) {
            this.features = features;
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((enhetsId == null) ? 0 : enhetsId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LegacyNavEnhetFrontend other = (LegacyNavEnhetFrontend) obj;
            if (enhetsId == null) {
                if (other.enhetsId != null)
                    return false;
            } else if (!enhetsId.equals(other.enhetsId))
                return false;
            return true;
        }
    }
}

package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/personalia/navEnheter")
@Timed
@Produces(APPLICATION_JSON)
public class NavEnhetRessurs {
    private static final Logger logger = LoggerFactory.getLogger(AdresseRessurs.class);

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SoknadsmottakerService soknadsmottakerService;

    @Inject
    private NorgService norgService;

    @GET
    public List<NavEnhetFrontend> hentNavEnheter(@PathParam("behandlingsId") String behandlingsId) {
        String eier = OidcFeatureToggleUtils.getUserId();
        JsonSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad().getSoknad();
        String valgtEnhetNr = soknad.getMottaker().getEnhetsnummer();

        JsonAdresse oppholdsadresse = soknad.getData().getPersonalia().getOppholdsadresse();
        String adresseValg = oppholdsadresse == null ? null :
                oppholdsadresse.getAdresseValg() == null ? null :
                        oppholdsadresse.getAdresseValg().toString();

        return findSoknadsmottaker(soknad, adresseValg, valgtEnhetNr);
    }

    @PUT
    public void updateNavEnhet(@PathParam("behandlingsId") String behandlingsId, NavEnhetFrontend navEnhetFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        soknad.getJsonInternalSoknad().setMottaker(new no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
                .withNavEnhetsnavn(navEnhetFrontend.enhetsnavn + ", " + navEnhetFrontend.kommunenavn)
                .withOrganisasjonsnummer(navEnhetFrontend.orgnr));
        soknad.getJsonInternalSoknad().getSoknad().setMottaker(new JsonSoknadsmottaker()
                .withNavEnhetsnavn(navEnhetFrontend.enhetsnavn + ", " + navEnhetFrontend.kommunenavn)
                .withEnhetsnummer(navEnhetFrontend.enhetsnr));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    public List<NavEnhetRessurs.NavEnhetFrontend> findSoknadsmottaker(JsonSoknad soknad, String valg, String valgtEnhetNr) {
        JsonPersonalia personalia = soknad.getData().getPersonalia();

        List<AdresseForslag> adresseForslagene = soknadsmottakerService.finnAdresseFraSoknad(personalia, valg);
        /*
         * Vi fjerner nå duplikate NAV-enheter med forskjellige bydelsnumre gjennom
         * bruk av distinct. Hvis det er viktig med riktig bydelsnummer bør dette kallet
         * fjernes og brukeren må besvare hvilken bydel han/hun oppholder seg i.
         */
        return adresseForslagene.stream().map((adresseForslag) -> {
            NavEnhet navEnhet = norgService.finnEnhetForGt(adresseForslag.geografiskTilknytning);
            return mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(adresseForslag, navEnhet, valgtEnhetNr);
        }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private NavEnhetRessurs.NavEnhetFrontend mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(AdresseForslag adresseForslag, NavEnhet navEnhet, String valgtEnhetNr) {
        if (navEnhet == null) {
            logger.warn("Kunne ikke hente NAV-enhet: " + adresseForslag.geografiskTilknytning);
            return null;
        }
        if (adresseForslag.kommunenummer == null
                || adresseForslag.kommunenummer.length() != 4) {
            return null;
        }

        boolean digisosKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(adresseForslag.kommunenummer);
        String sosialOrgnr = digisosKommune ? navEnhet.sosialOrgnr : null;
        String enhetNr = digisosKommune ? navEnhet.enhetNr : null;
        boolean valgt = enhetNr != null && enhetNr.equals(valgtEnhetNr);
        return new NavEnhetRessurs.NavEnhetFrontend()
                .withEnhetsnavn(navEnhet.navn)
                .withKommunenavn(adresseForslag.kommunenavn)
                .withOrgnr(sosialOrgnr)
                .withEnhetsnr(enhetNr)
                .withValgt(valgt);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NavEnhetFrontend {
        public String orgnr;
        public String enhetsnr;
        public String enhetsnavn;
        public String kommunenavn;
        public boolean valgt;

        public NavEnhetFrontend withOrgnr(String orgnr) {
            this.orgnr = orgnr;
            return this;
        }

        public NavEnhetFrontend withEnhetsnr(String enhetsnr) {
            this.enhetsnr = enhetsnr;
            return this;
        }

        public NavEnhetFrontend withEnhetsnavn(String enhetsnavn) {
            this.enhetsnavn = enhetsnavn;
            return this;
        }

        public NavEnhetFrontend withKommunenavn(String kommunenavn) {
            this.kommunenavn = kommunenavn;
            return this;
        }

        public NavEnhetFrontend withValgt(boolean valgt) {
            this.valgt = valgt;
            return this;
        }
    }
}

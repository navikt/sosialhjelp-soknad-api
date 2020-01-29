package no.nav.sbl.dialogarena.rest.ressurser.personalia;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadsmottakerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneInfoService;
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
@Path("/soknader/{behandlingsId}/personalia")
@Timed
@Produces(APPLICATION_JSON)
public class NavEnhetRessurs {
    private static final Logger log = LoggerFactory.getLogger(NavEnhetRessurs.class);
    private static final String SPLITTER = ", ";

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Inject
    private SoknadsmottakerService soknadsmottakerService;

    @Inject
    private NorgService norgService;

    @Inject
    private KommuneInfoService kommuneInfoService;

    @GET
    @Path("/navEnheter")
    public List<NavEnhetFrontend> hentNavEnheter(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getUserIdFromToken();
        JsonSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad().getSoknad();
        String valgtEnhetNr = soknad.getMottaker().getEnhetsnummer();

        JsonAdresse oppholdsadresse = soknad.getData().getPersonalia().getOppholdsadresse();
        String adresseValg = oppholdsadresse == null ? null :
                oppholdsadresse.getAdresseValg() == null ? null :
                        oppholdsadresse.getAdresseValg().toString();

        return findSoknadsmottaker(soknad, adresseValg, valgtEnhetNr);
    }

    @GET
    @Path("/navEnhet")
    public NavEnhetFrontend hentValgtNavEnhet(@PathParam("behandlingsId") String behandlingsId) {
        String eier = SubjectHandler.getUserIdFromToken();
        JsonSoknadsmottaker soknadsmottaker = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad().getSoknad().getMottaker();
        String kommunenummer = soknadsmottaker.getKommunenummer();

        if (kommunenummer == null || kommunenummer.isEmpty() ||
                soknadsmottaker.getNavEnhetsnavn() == null || soknadsmottaker.getNavEnhetsnavn().isEmpty()) {
            return null;
        }

        return new NavEnhetFrontend()
                .withEnhetsnr(soknadsmottaker.getEnhetsnummer())
                .withEnhetsnavn(getEnhetsnavnFromNavEnhetsnavn(soknadsmottaker.getNavEnhetsnavn()))
                .withKommunenavn(getKommunenavnFromNavEnhetsnavn(soknadsmottaker.getNavEnhetsnavn()))
                .withKommuneNr(kommunenummer)
                .withIsMottakDeaktivert(!isDigisosKommune(kommunenummer))
                .withIsMottakMidlertidigDeaktivert(kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer))
                .withOrgnr(KommuneTilNavEnhetMapper.getOrganisasjonsnummer(soknadsmottaker.getEnhetsnummer())) // Brukes ikke etter at kommunene er på Fiks konfigurasjon og burde ikke bli brukt av frontend.
                .withValgt(true);
    }

    @PUT
    @Path("/navEnheter")
    public void updateNavEnhet(@PathParam("behandlingsId") String behandlingsId, NavEnhetFrontend navEnhetFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = SubjectHandler.getUserIdFromToken();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        soknad.getJsonInternalSoknad().setMottaker(new no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
                .withNavEnhetsnavn(navEnhetFrontend.enhetsnavn + ", " + navEnhetFrontend.kommunenavn)
                .withOrganisasjonsnummer(navEnhetFrontend.orgnr));
        soknad.getJsonInternalSoknad().getSoknad().setMottaker(new JsonSoknadsmottaker()
                .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
                .withEnhetsnummer(navEnhetFrontend.enhetsnr)
                .withKommunenummer(navEnhetFrontend.kommuneNr));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    public String createNavEnhetsnavn(String enhetsnavn, String kommunenavn) {
        return enhetsnavn + SPLITTER + kommunenavn;
    }

    public String getEnhetsnavnFromNavEnhetsnavn(String navEnhetsnavn) {
        return navEnhetsnavn.split(SPLITTER)[0];
    }

    public String getKommunenavnFromNavEnhetsnavn(String navEnhetsnavn) {
        return navEnhetsnavn.split(SPLITTER)[1];
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
            log.warn("Kunne ikke hente NAV-enhet: " + adresseForslag.geografiskTilknytning);
            return null;
        }

        String kommunenummer = adresseForslag.kommunenummer;
        if (kommunenummer == null || kommunenummer.length() != 4) {
            return null;
        }

        if (!ServiceUtils.isRunningInProd() && MockUtils.isAlltidHentKommuneInfoFraNavTestkommune()) {
            log.error("Sender til Nav-testkommune (2352). Du skal aldri se denne meldingen i PROD");
            kommunenummer = "2352";
        }

        boolean digisosKommune = isDigisosKommune(kommunenummer);
        String sosialOrgnr = digisosKommune ? navEnhet.sosialOrgnr : null;
        String enhetNr = digisosKommune ? navEnhet.enhetNr : null;

        boolean valgt = enhetNr != null && enhetNr.equals(valgtEnhetNr);
        return new NavEnhetRessurs.NavEnhetFrontend()
                .withEnhetsnavn(navEnhet.navn)
                .withKommunenavn(adresseForslag.kommunenavn)
                .withOrgnr(sosialOrgnr)
                .withEnhetsnr(enhetNr)
                .withValgt(valgt)
                .withKommuneNr(kommunenummer)
                .withIsMottakMidlertidigDeaktivert(kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer))
                .withIsMottakDeaktivert(!digisosKommune);
    }

    private boolean isDigisosKommune(String kommunenummer){
        boolean isNyDigisosApiKommuneMedMottakAktivert = kommuneInfoService.kanMottaSoknader(kommunenummer) && ServiceUtils.isSendingTilFiksEnabled();
        boolean isGammelSvarUtKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(kommunenummer);
        return isNyDigisosApiKommuneMedMottakAktivert || isGammelSvarUtKommune;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NavEnhetFrontend {
        public String orgnr;
        public String enhetsnr;
        public String enhetsnavn;
        public String kommunenavn;
        public String kommuneNr;
        public boolean valgt;
        public boolean isMottakMidlertidigDeaktivert;
        public boolean isMottakDeaktivert;

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

        public NavEnhetFrontend withKommuneNr(String kommuneNr) {
            this.kommuneNr = kommuneNr;
            return this;
        }

        public NavEnhetFrontend withIsMottakMidlertidigDeaktivert(boolean isMottakMidlertidigDeaktivert) {
            this.isMottakMidlertidigDeaktivert = isMottakMidlertidigDeaktivert;
            return this;
        }

        public NavEnhetFrontend withIsMottakDeaktivert(boolean isMottakDeaktivert) {
            this.isMottakDeaktivert = isMottakDeaktivert;
            return this;
        }
    }
}

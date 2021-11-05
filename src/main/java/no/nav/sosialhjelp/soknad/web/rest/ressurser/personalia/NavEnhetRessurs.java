package no.nav.sosialhjelp.soknad.web.rest.ressurser.personalia;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag;
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslagType;
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseSokService;
import no.nav.sosialhjelp.soknad.consumer.fiks.KommuneInfoService;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelService;
import no.nav.sosialhjelp.soknad.consumer.pdl.geografisktilknytning.GeografiskTilknytningService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhet;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelService.BYDEL_MARKA;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/soknader/{behandlingsId}/personalia")
@Timed
@Produces(APPLICATION_JSON)
public class NavEnhetRessurs {

    private static final Logger log = LoggerFactory.getLogger(NavEnhetRessurs.class);
    private static final String SPLITTER = ", ";

    private final Tilgangskontroll tilgangskontroll;
    private final SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    private final NavEnhetService navEnhetService;
    private final KommuneInfoService kommuneInfoService;
    private final BydelService bydelService;
    private final AdresseSokService adresseSokService;
    private final GeografiskTilknytningService geografiskTilknytningService;
    private final KodeverkService kodeverkService;

    public NavEnhetRessurs(
            Tilgangskontroll tilgangskontroll,
            SoknadUnderArbeidRepository soknadUnderArbeidRepository,
            NavEnhetService navEnhetService,
            KommuneInfoService kommuneInfoService,
            BydelService bydelService,
            AdresseSokService adresseSokService,
            GeografiskTilknytningService geografiskTilknytningService,
            KodeverkService kodeverkService
    ) {
        this.tilgangskontroll = tilgangskontroll;
        this.soknadUnderArbeidRepository = soknadUnderArbeidRepository;
        this.navEnhetService = navEnhetService;
        this.kommuneInfoService = kommuneInfoService;
        this.bydelService = bydelService;
        this.adresseSokService = adresseSokService;
        this.geografiskTilknytningService = geografiskTilknytningService;
        this.kodeverkService = kodeverkService;
    }

    @GET
    @Path("/navEnheter")
    public List<NavEnhetFrontend> hentNavEnheter(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String eier = SubjectHandler.getUserId();
        JsonSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad().getSoknad();
        String valgtEnhetNr = soknad.getMottaker().getEnhetsnummer();

        JsonAdresse oppholdsadresse = soknad.getData().getPersonalia().getOppholdsadresse();
        String adresseValg = oppholdsadresse == null ? null :
                oppholdsadresse.getAdresseValg() == null ? null :
                        oppholdsadresse.getAdresseValg().toString();

        return findSoknadsmottaker(eier, soknad, adresseValg, valgtEnhetNr);
    }

    @GET
    @Path("/navEnhet")
    public NavEnhetFrontend hentValgtNavEnhet(@PathParam("behandlingsId") String behandlingsId) {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String eier = SubjectHandler.getUserId();
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
        String eier = SubjectHandler.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);

        soknad.getJsonInternalSoknad().setMottaker(new no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
                .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
                .withOrganisasjonsnummer(navEnhetFrontend.orgnr));
        soknad.getJsonInternalSoknad().getSoknad().setMottaker(new JsonSoknadsmottaker()
                .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
                .withEnhetsnummer(navEnhetFrontend.enhetsnr)
                .withKommunenummer(navEnhetFrontend.kommuneNr));

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private String createNavEnhetsnavn(String enhetsnavn, String kommunenavn) {
        return enhetsnavn + SPLITTER + kommunenavn;
    }

    private String getEnhetsnavnFromNavEnhetsnavn(String navEnhetsnavn) {
        return navEnhetsnavn.split(SPLITTER)[0];
    }

    private String getKommunenavnFromNavEnhetsnavn(String navEnhetsnavn) {
        return navEnhetsnavn.split(SPLITTER)[1];
    }

    public List<NavEnhetRessurs.NavEnhetFrontend> findSoknadsmottaker(String eier, JsonSoknad soknad, String valg, String valgtEnhetNr) {
        var personalia = soknad.getData().getPersonalia();

        if ("folkeregistrert".equals(valg)) {
            try {
                return finnNavEnhetFraGT(eier, personalia, valgtEnhetNr);
            } catch (Exception e) {
                log.warn("Noe feilet ved utleding av Nav-kontor ut fra GT hentet fra PDL -> fallback til adressesøk-løsning", e);
                return finnNavEnhetFraAdresse(personalia, valg, valgtEnhetNr);
            }
        }

        return finnNavEnhetFraAdresse(personalia, valg, valgtEnhetNr);
    }

    private List<NavEnhetRessurs.NavEnhetFrontend> finnNavEnhetFraGT(String ident, JsonPersonalia personalia, String valgtEnhetNr) {
        var kommunenummer = getKommunenummer(personalia.getOppholdsadresse());
        if (kommunenummer == null) {
            return Collections.emptyList();
        }

        var geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident);
        var navEnhet = navEnhetService.getEnhetForGt(geografiskTilknytning);
        var navEnhetFrontend = mapToNavEnhetFrontend(navEnhet, geografiskTilknytning, kommunenummer, valgtEnhetNr);
        return navEnhetFrontend == null ? Collections.emptyList() : Collections.singletonList(navEnhetFrontend);
    }

    private String getKommunenummer(JsonAdresse oppholdsadresse) {
        String kommunenummer = null;
        if (oppholdsadresse instanceof JsonMatrikkelAdresse) {
            kommunenummer = ((JsonMatrikkelAdresse) oppholdsadresse).getKommunenummer();
        }
        if (oppholdsadresse instanceof JsonGateAdresse) {
            kommunenummer = ((JsonGateAdresse) oppholdsadresse).getKommunenummer();
        }
        return kommunenummer;
    }

    private NavEnhetRessurs.NavEnhetFrontend mapToNavEnhetFrontend(NavEnhet navEnhet, String geografiskTilknytning, String kommunenummer, String valgtEnhetNr) {
        if (navEnhet == null) {
            log.warn("Kunne ikke hente NAV-enhet: {} , i kommune: {}", geografiskTilknytning, kommunenummer);
            return null;
        }

        if (kommunenummer == null || kommunenummer.length() != 4) {
            log.warn("Kommunenummer hadde ikke 4 tegn, var {}", kommunenummer);
            return null;
        }

        if (ServiceUtils.isNonProduction() && MockUtils.isAlltidHentKommuneInfoFraNavTestkommune()) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD");
            kommunenummer = "3002";
        }

        var isDigisosKommune = isDigisosKommune(kommunenummer);
        var sosialOrgnr = isDigisosKommune ? navEnhet.getSosialOrgNr() : null;
        var enhetNr = isDigisosKommune ? navEnhet.getEnhetNr() : null;

        var valgt = enhetNr != null && enhetNr.equals(valgtEnhetNr);

        var kommunenavn = kodeverkService.getKommunenavn(kommunenummer);

        return new NavEnhetRessurs.NavEnhetFrontend()
                .withEnhetsnavn(navEnhet.getNavn())
                .withKommunenavn(kommuneInfoService.getBehandlingskommune(kommunenummer, kommunenavn))
                .withOrgnr(sosialOrgnr)
                .withEnhetsnr(enhetNr)
                .withValgt(valgt)
                .withKommuneNr(kommunenummer)
                .withIsMottakMidlertidigDeaktivert(kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer))
                .withIsMottakDeaktivert(!isDigisosKommune);
    }

    private List<NavEnhetRessurs.NavEnhetFrontend> finnNavEnhetFraAdresse(JsonPersonalia personalia, String valg, String valgtEnhetNr) {
        List<AdresseForslag> adresseForslagene = adresseSokService.finnAdresseFraSoknad(personalia, valg);
        /*
         * Vi fjerner nå duplikate NAV-enheter med forskjellige bydelsnumre gjennom
         * bruk av distinct. Hvis det er viktig med riktig bydelsnummer bør dette kallet
         * fjernes og brukeren må besvare hvilken bydel han/hun oppholder seg i.
         */

        List<NavEnhetRessurs.NavEnhetFrontend> navEnhetFrontendListe = new ArrayList<>();

        for (AdresseForslag adresseForslag : adresseForslagene) {
            if (adresseForslag.type != null && adresseForslag.type.equals(AdresseForslagType.MATRIKKELADRESSE)) {
                List<NavEnhet> navenheter = navEnhetService.getEnheterForKommunenummer(adresseForslag.kommunenummer);
                navenheter.forEach(navEnhet -> addToNavEnhetFrontendListe(navEnhetFrontendListe, adresseForslag.geografiskTilknytning, adresseForslag, navEnhet, valgtEnhetNr));
                log.info("Matrikkeladresse ble brukt. Returnerer {} navenheter", navenheter.size());
            } else {
                var geografiskTilknytning = getGeografiskTilknytningFromAdresseForslag(adresseForslag);
                var navEnhet = navEnhetService.getEnhetForGt(geografiskTilknytning);
                addToNavEnhetFrontendListe(navEnhetFrontendListe, geografiskTilknytning, adresseForslag, navEnhet, valgtEnhetNr);
            }
        }

        return navEnhetFrontendListe.stream().distinct().collect(Collectors.toList());
    }

    private void addToNavEnhetFrontendListe(List<NavEnhetRessurs.NavEnhetFrontend> navEnhetFrontendListe, String geografiskTilknytning, AdresseForslag adresseForslag, NavEnhet navEnhet, String valgtEnhetNr) {
        NavEnhetFrontend navEnhetFrontend = mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(geografiskTilknytning, adresseForslag, navEnhet, valgtEnhetNr);
        if (navEnhetFrontend != null) {
            navEnhetFrontendListe.add(navEnhetFrontend);
        }
    }

    private NavEnhetRessurs.NavEnhetFrontend mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(String geografiskTilknytning, AdresseForslag adresseForslag, NavEnhet navEnhet, String valgtEnhetNr) {
        if (navEnhet == null) {
            log.warn("Kunne ikke hente NAV-enhet: {} , i kommune: {} ({})", geografiskTilknytning, adresseForslag.kommunenavn, adresseForslag.kommunenummer);
            return null;
        }

        String kommunenummer = adresseForslag.kommunenummer;
        if (kommunenummer == null || kommunenummer.length() != 4) {
            log.warn("Kommunenummer hadde ikke 4 tegn, var {}", kommunenummer);
            return null;
        }

        if (ServiceUtils.isNonProduction() && MockUtils.isAlltidHentKommuneInfoFraNavTestkommune()) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD");
            kommunenummer = "3002";
        }

        boolean digisosKommune = isDigisosKommune(kommunenummer);
        String sosialOrgnr = digisosKommune ? navEnhet.getSosialOrgNr() : null;
        String enhetNr = digisosKommune ? navEnhet.getEnhetNr() : null;

        boolean valgt = enhetNr != null && enhetNr.equals(valgtEnhetNr);
        String kommunenavnFraAdresseforslag = adresseForslag.kommunenavn != null ? adresseForslag.kommunenavn : navEnhet.getKommunenavn();
        return new NavEnhetRessurs.NavEnhetFrontend()
                .withEnhetsnavn(navEnhet.getNavn())
                .withKommunenavn(kommuneInfoService.getBehandlingskommune(kommunenummer, kommunenavnFraAdresseforslag))
                .withOrgnr(sosialOrgnr)
                .withEnhetsnr(enhetNr)
                .withValgt(valgt)
                .withKommuneNr(kommunenummer)
                .withIsMottakMidlertidigDeaktivert(kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer))
                .withIsMottakDeaktivert(!digisosKommune);
    }

    private boolean isDigisosKommune(String kommunenummer) {
        boolean isNyDigisosApiKommuneMedMottakAktivert = kommuneInfoService.kanMottaSoknader(kommunenummer) && ServiceUtils.isSendingTilFiksEnabled();
        boolean isGammelSvarUtKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(kommunenummer);
        return isNyDigisosApiKommuneMedMottakAktivert || isGammelSvarUtKommune;
    }

    private String getGeografiskTilknytningFromAdresseForslag(AdresseForslag adresseForslag) {
        if (BYDEL_MARKA.equals(adresseForslag.geografiskTilknytning)) {
            return bydelService.getBydelTilForMarka(adresseForslag);
        }
        // flere special cases her?
        return adresseForslag.geografiskTilknytning;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NavEnhetFrontend {
        public String orgnr;
        public String enhetsnr;
        public String enhetsnavn;
        public String kommunenavn;
        public String kommuneNr;
        public String behandlingsansvarlig;
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

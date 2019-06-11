package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.Logg;
import no.nav.sbl.dialogarena.rest.ressurser.personalia.NavEnhetRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/informasjon")
@Produces(APPLICATION_JSON)
@Timed
public class InformasjonRessurs {

    private static final Logger logger = LoggerFactory.getLogger(InformasjonRessurs.class);
    private static final Logger klientlogger = LoggerFactory.getLogger("klientlogger");

    private static final List<String> DISKRESJONSKODER = asList("6", "7");

    @Inject
    private InformasjonService informasjon;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private PersonService personService;
    @Inject
    private AdresseSokService adresseSokService;

    @GET
    @Path("/miljovariabler")
    public Map<String, String> hentMiljovariabler() {
        return informasjon.hentMiljovariabler();
    }

    @GET
    @Path("/fornavn")
    public Map<String, String> hentFornavn() {
        String fnr = OidcFeatureToggleUtils.getUserId();
        Person person = personService.hentPerson(fnr);
        if (person == null){
            return new HashMap<>();
        }
        String fornavn = person.getFornavn() != null ? person.getFornavn() : "";

        Map<String, String> fornavnMap = new HashMap<>();
        fornavnMap.put("fornavn", fornavn);
        return fornavnMap;
    }

    @Unprotected
    @GET
    @Path("/tekster")
    public Properties hentTekster(@QueryParam("type") String type, @QueryParam("sprak") String sprak) {
        if (sprak == null || sprak.trim().isEmpty()) {
            sprak = "nb_NO";
        }

        if(isNotEmpty(type) && !BUNDLE_NAME.equals(type.toLowerCase())){
            String prefiksetType = new StringBuilder("soknad").append(type.toLowerCase()).toString();
            logger.warn("Type {} matcher ikke et bundlename - forsøker med prefiks {}", type, prefiksetType);
            if(BUNDLE_NAME.equals(prefiksetType)){
                type = prefiksetType;
            }
        }

        Locale locale = LocaleUtils.toLocale(sprak);
        return messageSource.getBundleFor(type, locale);
    }

    @GET
    @Path("/utslagskriterier/sosialhjelp")
    public Map<String, Object> hentAdresse() {
        String uid = OidcFeatureToggleUtils.getUserId();
        Person person = personService.hentPerson(uid);

        Map<String, Object> resultat = new HashMap<>();

        boolean harTilgang = true;
        String sperrekode = "";

        if (DISKRESJONSKODER.contains(person.getDiskresjonskode())) {
            harTilgang = false;
            sperrekode = "bruker";
        }

        resultat.put("harTilgang", harTilgang);
        resultat.put("sperrekode", sperrekode);

        return resultat;
    }

    @POST
    @Path("/actions/logg")
    public void loggFraKlient(Logg logg) {
        String level = logg.getLevel();

        switch (level) {
            case "INFO":
                klientlogger.info(logg.melding());
                break;
            case "WARN":
                klientlogger.warn(logg.melding());
                break;
            case "ERROR":
                klientlogger.error(logg.melding());
                break;
            default:
                klientlogger.debug(logg.melding());
                break;
        }
    }

    @GET
    @Path("/tilgjengelige_kommuner")
    public List<String> hentTilgjengeligeKommuner() {
        return KommuneTilNavEnhetMapper.getDigisoskommuner();
    }

    @GET
    @Path("/kommunesok")
    public List<NavEnhetRessurs.NavEnhetFrontend> sokEtterNavEnheter(@QueryParam("kommunenr") String kommunenr) {
        return adresseSokService.sokEtterNavEnheter(kommunenr).stream()
                .map(this::mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend)
                .collect(Collectors.toList());
    }

    private NavEnhetRessurs.NavEnhetFrontend mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(AdresseSokService.Kommunesok kommunesok) {
        if (kommunesok.navEnhet == null) {
            logger.warn("Kunne ikke hente NAV-enhet: " + kommunesok.adresseForslag.geografiskTilknytning);
            return null;
        }
        boolean digisosKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(kommunesok.kommunenr);
        String kommunenavn = KommuneTilNavEnhetMapper.IKS_KOMMUNER.getOrDefault(kommunesok.kommunenr, kommunesok.adresseForslag.kommunenavn);
        return new NavEnhetRessurs.NavEnhetFrontend()
                .withEnhetsnavn(kommunesok.navEnhet.navn)
                .withKommunenavn(kommunenavn)
                .withOrgnr((digisosKommune) ? kommunesok.navEnhet.sosialOrgnr : null);
    }

}
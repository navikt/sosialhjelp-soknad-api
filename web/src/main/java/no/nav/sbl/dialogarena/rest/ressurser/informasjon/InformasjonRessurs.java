package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.Logg;
import no.nav.sbl.dialogarena.rest.ressurser.personalia.NavEnhetRessurs;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.DigisosApi;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneInfoService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.utils.NedetidUtils;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
@Path("/informasjon")
@Produces(APPLICATION_JSON)
@Timed
public class InformasjonRessurs {

    private static final Logger logger = LoggerFactory.getLogger(InformasjonRessurs.class);
    private static final Logger klientlogger = LoggerFactory.getLogger("klientlogger");

    private static final List<String> DISKRESJONSKODER = asList("6", "7");

    private final InformasjonService informasjon;
    private final NavMessageSource messageSource;
    private final PersonService personService;
    private final AdresseSokService adresseSokService;
    private final DigisosApi digisosApi;
    private final KommuneInfoService kommuneInfoService;

    public InformasjonRessurs(InformasjonService informasjon, NavMessageSource messageSource, PersonService personService, AdresseSokService adresseSokService, DigisosApi digisosApi, KommuneInfoService kommuneInfoService) {
        this.informasjon = informasjon;
        this.messageSource = messageSource;
        this.personService = personService;
        this.adresseSokService = adresseSokService;
        this.digisosApi = digisosApi;
        this.kommuneInfoService = kommuneInfoService;
    }

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
        if (person == null) {
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

        if (isNotEmpty(type) && !BUNDLE_NAME.equals(type.toLowerCase())) {
            String prefiksetType = "soknad" + type.toLowerCase();
            logger.warn("Type {} matcher ikke et bundlename - forsøker med prefiks {}", type, prefiksetType);
            if (BUNDLE_NAME.equals(prefiksetType)) {
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

    @GET
    @Path("/adressesok")
    public List<AdresseForslag> adresseSok(@QueryParam("sokestreng") String sokestreng) {
        return adresseSokService.sokEtterAdresser(sokestreng);
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

    @Unprotected
    @GET
    @Path("/tilgjengelige_kommuner")
    public Set<String> hentTilgjengeligeKommuner() {
        if (NedetidUtils.isInnenforNedetid()) {
            return new HashSet<>();
        }
        List<String> manueltPaakobledeKommuner = KommuneTilNavEnhetMapper.getDigisoskommuner();

        Set<String> digisosApiKommuner = digisosApi.hentKommuneInfo().keySet().stream()
                .filter(kommuneInfoService::kanMottaSoknader)
                .collect(Collectors.toSet());

        Set<String> union = new HashSet<>(manueltPaakobledeKommuner);
        union.addAll(digisosApiKommuner);

        return union;
    }

    @Unprotected
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
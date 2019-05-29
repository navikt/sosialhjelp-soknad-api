package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.rest.Logg;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseForslag;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet.Kontaktinformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse.AdresseSokService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeid.ArbeidssokerInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.utils.InnloggetBruker;
import no.nav.security.oidc.api.ProtectedWithClaims;
import no.nav.security.oidc.api.Unprotected;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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
    private InnloggetBruker innloggetBruker;
    @Inject
    private Kodeverk kodeverk;
    @Inject
    private LandService landService;
    @Inject
    private PersonaliaFletter personaliaFletter;
    @Inject
    private ArbeidssokerInfoService arbeidssokerInfoService;
    @Inject
    private PersonInfoService personInfoService;
    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;
    @Inject
    private AdresseSokService adresseSokService;
    @Inject
    private NorgService norgService;

    @GET
    @Path("/miljovariabler")
    public Map<String, String> hentMiljovariabler() {
        return informasjon.hentMiljovariabler();
    }

    @GET
    @Path("/personalia")
    public Personalia hentKunFornavnPersonalia() {
        return innloggetBruker.hentKunFornavnPersonalia();
    }

    @GET
    @Path("/fornavn")
    public String hentFornavn() {
        return innloggetBruker.hentFornavn();
    }

    @GET
    @Path("/fornavn")
    public String hentFornavn() {
        return innloggetBruker.hentFornavn();
    }

    @GET
    @Path("/poststed")
    @Produces("text/plain")
    public String hentPoststed(@QueryParam("postnummer") String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }

    @Unprotected
    @GET
    @Path("/tekster")
    public Properties hentTekster(@QueryParam("type") String type, @QueryParam("sprak") String sprak) {
        if (sprak == null || sprak.trim().isEmpty()) {
            sprak = "nb_NO";
        }

        List<String> bundleNames = kravdialogInformasjonHolder.getSoknadsKonfigurasjoner().stream()
                .map(k -> k.getBundleName())
                .collect(toList());

        if(isNotEmpty(type) && !bundleNames.contains(type.toLowerCase())){
            String prefiksetType = new StringBuilder("soknad").append(type.toLowerCase()).toString();
            logger.warn("Type {} matcher ikke et bundlename - forsøker med prefiks {}", type, prefiksetType);
            if(bundleNames.contains(prefiksetType)){
                type = prefiksetType;
            }
        }

        Locale locale = LocaleUtils.toLocale(sprak);
        return messageSource.getBundleFor(type, locale);
    }

    @GET
    @Path("/land")
    public List<Land> hentLand(@QueryParam("filter") String filter) {
        return landService.hentLand(filter);
    }

    @GET
    @Path("/land/actions/hentstatsborgerskapstype")
    public Map<String, String> hentStatsborgerskapstype(@QueryParam("landkode") String landkode) {
        return landService.hentStatsborgerskapstype(landkode);
    }

    @GET
    @Path("/kodeverk")
    public Map<String, String> hentKodeverk(@QueryParam("kodeverk") Kodeverk.EksponertKodeverk kodeverkKey) {
        return kodeverk.hentAlleKodenavnMedForsteTerm(kodeverkKey);
    }

    @GET
    @Path("/utslagskriterier")
    public Map<String, Object> hentUtslagskriterier() {
        String uid = OidcFeatureToggleUtils.getUserId();
        Map<String, Object> utslagskriterierResultat = new HashMap<>();
        utslagskriterierResultat.put("arbeidssokerstatus", personInfoService.hentArbeidssokerStatus(uid));
        utslagskriterierResultat.put("arbeidssokertatusFraSBLArbeid", arbeidssokerInfoService.getArbeidssokerArenaStatus(uid));
        utslagskriterierResultat.put("ytelsesstatus", personInfoService.hentYtelseStatus(uid));

        try {
            Personalia personalia = personaliaFletter.mapTilPersonalia(uid);
            utslagskriterierResultat.put("alder", Integer.toString(new PersonAlder(uid).getAlder()));
            utslagskriterierResultat.put("fodselsdato", personalia.getFodselsdato());
            utslagskriterierResultat.put("bosattINorge", ((Boolean) !personalia.harUtenlandskAdresse()).toString());
            utslagskriterierResultat.put("registrertAdresse", personalia.getGjeldendeAdresse().getAdresse());
            utslagskriterierResultat.put("registrertAdresseGyldigFra", personalia.getGjeldendeAdresse().getGyldigFra());
            utslagskriterierResultat.put("registrertAdresseGyldigTil", personalia.getGjeldendeAdresse().getGyldigTil());
            utslagskriterierResultat.put("erBosattIEOSLand", personalia.erBosattIEOSLand());
            utslagskriterierResultat.put("statsborgerskap", personalia.getStatsborgerskap());

        } catch (Exception e) {
            logger.error("Kunne ikke hente personalia", e);
            utslagskriterierResultat.put("error", e.getMessage());
        }
        return utslagskriterierResultat;
    }

    @GET
    @Path("/utslagskriterier/alder")
    public int hentAlder() {
        String uid = OidcFeatureToggleUtils.getUserId();

        return new PersonAlder(uid).getAlder();
    }

    @GET
    @Path("/utslagskriterier/sosialhjelp")
    public Map<String, Object> hentAdresse() {
        String uid = OidcFeatureToggleUtils.getUserId();
        Personalia personalia = personaliaFletter.mapTilPersonalia(uid);

        Map<String, Object> resultat = new HashMap<>();

        boolean harTilgang = true;
        String sperrekode = "";

        if (DISKRESJONSKODER.contains(personalia.getDiskresjonskode())) {
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

    @GET
    @Path("/enhet/geografisktilknytning")
    public NavEnhet finnEnhet(@QueryParam("gt") String gt) {
        return norgService.finnEnhetForGt(gt);
    }

    @GET
    @Path("/enhet/kontaktinfo")
    public Kontaktinformasjon kontaktInfo(@QueryParam("enhetId") String enhetId) {
        return norgService.hentKontaktInformasjon(enhetId);
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
    public List<String> hentAktiviteter() {
        return KommuneTilNavEnhetMapper.getDigisoskommuner();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @SuppressWarnings("unused")
    private static class NavEnhetFrontend {
        public String id;
        public String orgnr;
        public String navn;
        public String kommuneId;
        public String fulltNavn;
        public String type;
        public Map<String, Boolean> features;
        
        private NavEnhetFrontend(String id, String orgnr, String navn, String kommuneId, String fulltNavn, String type, Map<String, Boolean> features) {
            this.id = id;
            this.orgnr = orgnr;
            this.navn = navn;
            this.kommuneId = kommuneId;
            this.fulltNavn = fulltNavn;
            this.type = type;
            this.features = features;
        }
    }   
}
package no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.service.InformasjonService;
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag;
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseSokService;
import no.nav.sosialhjelp.soknad.business.service.informasjon.PabegynteSoknaderService;
import no.nav.sosialhjelp.soknad.client.fiks.KommuneInfoService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper;
import no.nav.sosialhjelp.soknad.person.PersonService;
import no.nav.sosialhjelp.soknad.person.dto.Gradering;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import no.nav.sosialhjelp.soknad.web.rest.Logg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.NyligInnsendteSoknaderResponse;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.informasjon.dto.PabegyntSoknad;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import no.nav.sosialhjelp.soknad.web.utils.NedetidUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/informasjon")
@Produces(APPLICATION_JSON)
@Timed
public class InformasjonRessurs {

    private static final Logger logger = LoggerFactory.getLogger(InformasjonRessurs.class);
    private static final Logger klientlogger = LoggerFactory.getLogger("klientlogger");
    private static final int FJORTEN_DAGER = 14;

    private final InformasjonService informasjon;
    private final NavMessageSource messageSource;
    private final AdresseSokService adresseSokService;
    private final KommuneInfoService kommuneInfoService;
    private final PersonService personService;
    private final Tilgangskontroll tilgangskontroll;
    private final SoknadMetadataRepository soknadMetadataRepository;
    private final PabegynteSoknaderService pabegynteSoknaderService;

    public InformasjonRessurs(
            InformasjonService informasjon,
            NavMessageSource messageSource,
            AdresseSokService adresseSokService,
            KommuneInfoService kommuneInfoService,
            PersonService personService,
            Tilgangskontroll tilgangskontroll,
            SoknadMetadataRepository soknadMetadataRepository,
            PabegynteSoknaderService pabegynteSoknaderService
            ) {
        this.informasjon = informasjon;
        this.messageSource = messageSource;
        this.adresseSokService = adresseSokService;
        this.kommuneInfoService = kommuneInfoService;
        this.personService = personService;
        this.tilgangskontroll = tilgangskontroll;
        this.soknadMetadataRepository = soknadMetadataRepository;
        this.pabegynteSoknaderService = pabegynteSoknaderService;
    }

    @GET
    @Path("/miljovariabler")
    public Map<String, String> hentMiljovariabler() {
        return informasjon.hentMiljovariabler();
    }

    @GET
    @Path("/fornavn")
    public Map<String, String> hentFornavn() {
        String fnr = SubjectHandler.getUserId();
        var person = personService.hentPerson(fnr);
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
    public Map<String, Object> getUtslagskriterier() {
        String uid = SubjectHandler.getUserId();
        var adressebeskyttelse = personService.hentAdressebeskyttelse(uid);

        Map<String, Object> resultat = new HashMap<>();

        boolean harTilgang = true;
        String sperrekode = "";

        if (Gradering.FORTROLIG.equals(adressebeskyttelse) || Gradering.STRENGT_FORTROLIG.equals(adressebeskyttelse) || Gradering.STRENGT_FORTROLIG_UTLAND.equals(adressebeskyttelse)) {
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

    @GET
    @Path("/kommunelogg")
    public String triggeKommunelogg(@QueryParam("kommunenummer") String kommunenummer) {
        logger.info("Kommuneinfo trigget for {}: {}", kommunenummer, kommuneInfoService.kommuneInfo(kommunenummer));
        return kommunenummer + " er logget. Sjekk kibana";
    }

    @Unprotected
    @GET
    @Path("/kommuneinfo")
    public Map<String, KommuneInfoFrontend> hentKommuneinfo() {
        if (NedetidUtils.isInnenforNedetid()) {
            return new HashMap<>();
        }
        Map<String, KommuneInfoFrontend> manueltPakobledeKommuner = mapManueltPakobledeKommuner(KommuneTilNavEnhetMapper.getDigisoskommuner());

        Map<String, KommuneInfoFrontend> digisosKommuner = mapDigisosKommuner(kommuneInfoService.hentAlleKommuneInfo());

        return mergeManuelleKommunerMedDigisosKommuner(manueltPakobledeKommuner, digisosKommuner);
    }

    @GET
    @Path("/harNyligInnsendteSoknader")
    public NyligInnsendteSoknaderResponse harNyligInnsendteSoknader() {
        tilgangskontroll.verifiserAtBrukerHarTilgang();

        var eier = SubjectHandler.getUserId();
        var grense = LocalDateTime.now().minusDays(FJORTEN_DAGER);
        var nyligSendteSoknader = soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(eier, grense);

        var antallNyligInnsendte = nyligSendteSoknader == null ? 0 : nyligSendteSoknader.size();
        return new NyligInnsendteSoknaderResponse(antallNyligInnsendte);
    }

    @GET
    @Path("/pabegynteSoknader")
    public List<PabegyntSoknad> hentPabegynteSoknader() {
        tilgangskontroll.verifiserAtBrukerHarTilgang();
        String fnr = SubjectHandler.getUserId();
        logger.debug("Henter pabegynte soknader for bruker");

        return pabegynteSoknaderService.hentPabegynteSoknaderForBruker(fnr);
    }

    public Map<String, KommuneInfoFrontend> mapManueltPakobledeKommuner(List<String> manuelleKommuner) {
        return manuelleKommuner.stream()
                .map(kommunenr -> new KommuneInfoFrontend()
                        .withKommunenummer(kommunenr)
                        .withKanMottaSoknader(true)
                        .withKanOppdatereStatus(false))
                .collect(Collectors.toMap(KommuneInfoFrontend::getKommunenummer, kommuneInfoFrontend -> kommuneInfoFrontend));
    }

    public Map<String, KommuneInfoFrontend> mapDigisosKommuner(Map<String, KommuneInfo> digisosKommuner) {
        return digisosKommuner.values().stream()
                .filter(KommuneInfo::getKanMottaSoknader)
                .map(kommuneInfo -> new KommuneInfoFrontend()
                        .withKommunenummer(kommuneInfo.getKommunenummer())
                        .withKanMottaSoknader(kommuneInfo.getKanMottaSoknader() && !kommuneInfo.getHarMidlertidigDeaktivertMottak())
                        .withKanOppdatereStatus(kommuneInfo.getKanOppdatereStatus()))
                .collect(Collectors.toMap(KommuneInfoFrontend::getKommunenummer, kommuneInfoFrontend -> kommuneInfoFrontend));
    }

    public Map<String, KommuneInfoFrontend> mergeManuelleKommunerMedDigisosKommuner(Map<String, KommuneInfoFrontend> manuelleKommuner, Map<String, KommuneInfoFrontend> digisosKommuner) {
        manuelleKommuner.forEach(digisosKommuner::putIfAbsent);
        return digisosKommuner;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class KommuneInfoFrontend {
        public String kommunenummer;
        public boolean kanMottaSoknader;
        public boolean kanOppdatereStatus;

        public String getKommunenummer() {
            return kommunenummer;
        }

        public KommuneInfoFrontend withKommunenummer(String kommunenummer) {
            this.kommunenummer = kommunenummer;
            return this;
        }

        public KommuneInfoFrontend withKanMottaSoknader(boolean kanMottaSoknader) {
            this.kanMottaSoknader = kanMottaSoknader;
            return this;
        }

        public KommuneInfoFrontend withKanOppdatereStatus(boolean kanOppdatereStatus) {
            this.kanOppdatereStatus = kanOppdatereStatus;
            return this;
        }
    }
}
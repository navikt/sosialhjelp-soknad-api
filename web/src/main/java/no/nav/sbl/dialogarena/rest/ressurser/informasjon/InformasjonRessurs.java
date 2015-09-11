package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.rest.Logg;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import no.nav.sbl.dialogarena.utils.InnloggetBruker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Controller
@Path("/informasjon")
@Produces(APPLICATION_JSON)
public class InformasjonRessurs {

    private static final Logger logger = LoggerFactory.getLogger(InformasjonRessurs.class);
    private static final Logger klientlogger = LoggerFactory.getLogger("klientlogger");

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
    private PersonaliaService personaliaService;
    @Inject
    private PersonInfoService personInfoService;
    @Inject
    private WebSoknadConfig webSoknadConfig;
    @Inject
    private TjenesterRessurs tjenesterRessurs;

    @Path("/tjenester")
    public Object getTjenesterRessurs(){
        return tjenesterRessurs;
    }

    @GET
    @Path("/miljovariabler")
    public Map<String, String> hentMiljovariabler() {
        return informasjon.hentMiljovariabler();
    }

    @GET
    @Path("/vedleggsskjema")
    public Map<String, String> hentVedleggsskjema(@QueryParam("type") String type, @QueryParam("behandlingsId") String behandlingsId) {
        if (type != null) {
            return informasjon.hentVedleggsskjema(type);
        } else {
            return informasjon.hentVedleggsskjemaForBehandlingsId(behandlingsId);
        }
    }

    @GET
    @Path("/personalia")
    public Personalia hentPersonalia() {
        return innloggetBruker.hentPersonalia();
    }

    @GET
    @Path("/poststed")
    @Produces("text/plain")
    public String hentPoststed(@QueryParam("postnummer") String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }

    @GET
    @Path("/tekster")
    public Properties hentTekster(@QueryParam("type") String type, @QueryParam("sprak") String sprak) {
        if (sprak == null || sprak.trim().isEmpty()) {
            sprak = "nb_NO";
        }

        if (!sprak.matches("[a-z][a-z]_[A-Z][A-Z]")) {
            throw new IllegalArgumentException("Språk må matche xx_XX: " + sprak);
        }

        String[] split = sprak.split("_");
        Locale locale = new Locale(split[0], split[1]);

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
    @Path("/soknadstruktur")
    public SoknadStruktur hentSoknadStruktur(@QueryParam("skjemanummer") String skjemanummer, @QueryParam("filter") String filter) {
        SoknadStruktur soknadStruktur = webSoknadConfig.hentStruktur(skjemanummer);
        if ("temakode".equalsIgnoreCase(filter)) {
            SoknadStruktur miniSoknadstruktur = new SoknadStruktur();
            miniSoknadstruktur.setTemaKode(soknadStruktur.getTemaKode());
            return miniSoknadstruktur;
        }
        return soknadStruktur;
    }

    @GET
    @Path("/utslagskriterier")
    public Map<String, Object> hentUtslagskriterier() {
        String uid = getSubjectHandler().getUid();
        Map<String, Object> utslagskriterierResultat = new HashMap<>();
        utslagskriterierResultat.put("arbeidssokerstatus", personInfoService.hentArbeidssokerStatus(uid));
        utslagskriterierResultat.put("ytelsesstatus", personInfoService.hentYtelseStatus(uid));

        try {
            Personalia personalia = personaliaService.hentPersonalia(uid);
            utslagskriterierResultat.put("alder", Integer.toString(new PersonAlder(uid).getAlder()));
            utslagskriterierResultat.put("fodselsdato", personalia.getFodselsdato());
            utslagskriterierResultat.put("bosattINorge", ((Boolean) !personalia.harUtenlandskAdresse()).toString());
            utslagskriterierResultat.put("registrertAdresse", personalia.getGjeldendeAdresse().getAdresse());
            utslagskriterierResultat.put("registrertAdresseGyldigFra", personalia.getGjeldendeAdresse().getGyldigFra());
            utslagskriterierResultat.put("registrertAdresseGyldigTil", personalia.getGjeldendeAdresse().getGyldigTil());
            utslagskriterierResultat.put("erBosattIEOSLand", personalia.erBosattIEOSLand());

        } catch (Exception e) {
            logger.error("Kunne ikke hente personalia", e);
            utslagskriterierResultat.put("error", e.getMessage());
        }
        return utslagskriterierResultat;
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



}
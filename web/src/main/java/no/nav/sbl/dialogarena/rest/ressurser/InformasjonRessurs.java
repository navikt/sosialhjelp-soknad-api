package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import no.nav.sbl.dialogarena.websoknad.servlet.InnloggetBruker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Path("/informasjon")
@Produces(APPLICATION_JSON)
public class InformasjonRessurs {

    private static final Logger logger = LoggerFactory.getLogger(InformasjonRessurs.class);

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
    public String hentPoststed(@QueryParam("postnummer") String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }

    @GET
    @Path("/tekster")
    public Properties hentTekster(@QueryParam("type") String type, @QueryParam("sprak") String sprak) {
        return messageSource.getBundleFor(type, new Locale("nb", "NO"));
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
    @Path("/utslagskriterier")
    public Map<String, String> sjekkUtslagskriterier() {
        String uid = getSubjectHandler().getUid();
        PersonInfoService.Status status = personInfoService.hentArbeidssokerStatus(uid);
        Map<String, String> utslagskriterierResultat = new HashMap<>();
        utslagskriterierResultat.put("registrertArbeidssøker", status.name());

        try {
            Personalia personalia = personaliaService.hentPersonalia(uid);
            utslagskriterierResultat.put("gyldigAlder", new PersonAlder(uid).sjekkAlder().toString());
            utslagskriterierResultat.put("bosattINorge", ((Boolean) !personalia.harUtenlandskAdresse()).toString());
            utslagskriterierResultat.put("registrertAdresse", personalia.getGjeldendeAdresse().getAdresse());
            utslagskriterierResultat.put("registrertAdresseGyldigFra", personalia.getGjeldendeAdresse().getGyldigFra());
            utslagskriterierResultat.put("registrertAdresseGyldigTil", personalia.getGjeldendeAdresse().getGyldigTil());
        } catch (Exception e) {
            logger.error("Kunne ikke hente personalia", e);
            utslagskriterierResultat.put("error", e.getMessage());
        }
        return utslagskriterierResultat;
    }

}
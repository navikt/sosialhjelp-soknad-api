package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Controller
@ControllerAdvice()
@RequestMapping(value = "/informasjon")
public class InformasjonController {

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

    @RequestMapping(value = "/miljovariabler", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Map<String, String> hentMiljovariabler() {
        return informasjon.hentMiljovariabler();
    }

    @RequestMapping(value = "/vedleggsskjema", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Map<String, String> hentVedleggsskjema(@RequestParam String type) { return informasjon.hentVedleggsskjema(type); }

    @RequestMapping(value = "/personalia", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Personalia hentPersonalia() { return innloggetBruker.hentPersonalia(); }

    @RequestMapping(value = "/poststed", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public String hentPoststed(@RequestParam String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }

    @RequestMapping(value = "/tekster", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Properties hentTekster(@RequestParam String type, @RequestParam String sprak) { return messageSource.getBundleFor(type, new Locale("nb", "NO")); }

    @RequestMapping(value = "/land", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public List<Land> hentLand(@RequestParam(required = false) String filter) {
        return landService.hentLand(filter);
    }

    @RequestMapping(value = "/land/actions/hentstatsborgerskapstype", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, String> hentStatsborgerskapstype(@RequestParam String landkode) {
        return landService.hentStatsborgerskapstype(landkode);
    }

}
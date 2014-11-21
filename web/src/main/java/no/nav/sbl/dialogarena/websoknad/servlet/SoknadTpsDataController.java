package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@ControllerAdvice()
@RequestMapping("/soknad")
public class SoknadTpsDataController {

    @Inject
    private Kodeverk kodeverk;

    @Inject
    private PersonaliaService personaliaService;

    @RequestMapping(value = "/kodeverk/{postnummer}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public String hentPoststed(@PathVariable String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }

    private static final Logger logger = getLogger(SoknadTpsDataController.class);

    @RequestMapping(value = "/kodeverk/landliste", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, List<Map<String, String>>> hentLandkodeListe() {

        List<Map<String, String>> landliste = new ArrayList<>();

        List<String> landKoder = kodeverk.getAlleLandkoder();

        for (String landkode : landKoder) {
            Map<String, String> land = new LinkedHashMap<>();
            land.put("text", kodeverk.getLand(landkode));
            land.put("value", landkode);
            landliste.add(land);
        }
        if (!landKoder.contains("NOR")) {
            Map<String, String> norge = new LinkedHashMap<>();
            norge.put("text", "Norge");
            norge.put("value", "NOR");
            landliste.add(norge);
        }
        Map<String, List<Map<String, String>>> resultMap = new LinkedHashMap<>();
        resultMap.put("result", landliste);
        return resultMap;
    }

    @RequestMapping(value = "/personalia/{soknadId}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @SjekkTilgangTilSoknad
    public void lagrePersonaliaOgBarn(@PathVariable Long soknadId, @RequestBody(required = false) Boolean lagreBarn) {
        String fnr = SubjectHandler.getSubjectHandler().getUid();
        Boolean skalLagreBarn = lagreBarn;
        if (skalLagreBarn == null) {
            skalLagreBarn = false;
        }
        personaliaService.lagrePersonaliaOgBarn(fnr, soknadId, skalLagreBarn);
    }

    @RequestMapping(value = "/personalia", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Personalia hentPersonalia() {
        String fnr = SubjectHandler.getSubjectHandler().getUid();
        Personalia personalia = null;
        try {
            personalia = personaliaService.hentPersonalia(fnr);
        } catch (Exception e) {
            logger.error("Kunne ikke hente personalia");
        }
        return personalia;
    }
}

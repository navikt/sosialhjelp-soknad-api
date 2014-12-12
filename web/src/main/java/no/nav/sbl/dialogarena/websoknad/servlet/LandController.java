package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@ControllerAdvice()
@RequestMapping("/land")
public class LandController {
    @Inject
    private Kodeverk kodeverk;

    @Inject
    private LandService landService;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, List<Map<String, String>>> hentAlleLand() {

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

    @RequestMapping(value = "/statsborgerskap/type/{landkode}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Map<String, String> hentStatsborgerskapstype(@PathVariable String landkode) {
        HashMap<String, String> result = new HashMap<>();
        result.put("result", String.valueOf(landService.getStatsborgeskapType(landkode)));
        return result;
    }

    @RequestMapping(value = "/eos", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public List<Map<String, String>> hentEosLand() {
        List<String> eosLand = landService.getEosLand();
        List<Map<String, String>> landliste = new ArrayList<>();

        for (String landkode : eosLand) {
            Map<String, String> land = new LinkedHashMap<>();
            String landnavn = kodeverk.getLand(landkode);
            landnavn = landnavn != null ? landnavn : landkode;
            land.put("text", landnavn);
            land.put("value", landkode);
            landliste.add(land);
        }
        return landliste;
    }
}

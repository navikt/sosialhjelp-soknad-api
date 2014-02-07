package no.nav.sbl.dialogarena.websoknad.servlet;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.EosBorgerService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@Controller
@ControllerAdvice()
@RequestMapping(value = "")
public class EosController {

    @Inject
    private EosBorgerService eosService;

    @RequestMapping(value = "/ereosland/{landkode}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Map<String, String> isEosLandAnnetEnnNorge(@PathVariable String landkode) {
        HashMap<String, String> result = new HashMap<>();
        result.put("result", String.valueOf(eosService.isEosLandAnnetEnnNorge(landkode)));
        return result;
    }
    
    @RequestMapping(value = "/landtype/{landkode}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Map<String, String> landType(@PathVariable String landkode) {
        HashMap<String, String> result = new HashMap<>();
        result.put("result", String.valueOf(eosService.getStatsborgeskapType(landkode)));
        return result;
    }
}

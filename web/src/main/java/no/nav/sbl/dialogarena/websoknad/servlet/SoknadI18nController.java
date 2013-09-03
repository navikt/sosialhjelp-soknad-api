package no.nav.sbl.dialogarena.websoknad.servlet;

/**
 * Controller for å hente oversettelser til bruk i en søknad.
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/i18n", method = RequestMethod.GET, produces = "application/json")
public class SoknadI18nController {

    @RequestMapping("soknad/{soknad}")
    @ResponseBody
    public Map<String, String> forSoknad(@PathVariable String soknad) {
        HashMap<String, String> result = new HashMap<>();
        result.put("key1", "oversetting1");
        result.put("key2", "oversetting2");
        return result;
    }

    @RequestMapping("nokkel/{nokkel}")
    @ResponseBody
    public String forNokkel(@PathVariable String nokkel) {
        return "nokkel:" + nokkel;
    }
}

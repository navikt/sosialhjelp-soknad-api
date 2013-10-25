package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.websoknad.config.NavMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Controller
@RequestMapping("/enonic")
public class MessageController {

    private Map<String, String> tekster = new LinkedHashMap<>();
    @Inject
    private NavMessageSource navMessageSource;

    @RequestMapping(value = "/{side}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Properties hentTekster(@PathVariable String side, Locale locale) {
        return navMessageSource.getBundleFor(side, new Locale("nb", "NO"));
    }
}

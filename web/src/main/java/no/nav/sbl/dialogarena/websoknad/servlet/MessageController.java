package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.websoknad.config.NavMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Properties;

@Controller
@RequestMapping("/enonic")
public class MessageController {

    @Inject
    private NavMessageSource navMessageSource;

    private boolean tvingNorsk = true;

    @RequestMapping(value = "/{side}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Properties hentTekster(@PathVariable String side, Locale locale) {
        Locale loc = locale;
        if (tvingNorsk) {
            loc = new Locale("nb", "NO");
        }

        return navMessageSource.getBundleFor(side, loc);
    }

    public void setTvingNorsk(boolean tvingNorsk) {
        this.tvingNorsk = tvingNorsk;
    }
}

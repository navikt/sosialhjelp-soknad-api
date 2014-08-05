package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Properties;

@Controller
@RequestMapping("/")
public class MessageController {

    @Inject
    private NavMessageSource navMessageSource;

    private boolean tvingNorsk = true;

    // TODO: Endre URL til å ikke inneholde enonic
    @RequestMapping(value = "/enonic/{side}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Properties hentTekster(@PathVariable String side, Locale locale) {
        Locale loc = locale;
        if (tvingNorsk) {
            loc = new Locale("nb", "NO");
        }
        return navMessageSource.getBundleFor(side, loc);
    }

    @RequestMapping(value = "/messages/{kode}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody()
    public String hentTekst(@PathVariable String kode, Locale locale) {
        Locale loc = locale;
        if (tvingNorsk) {
            loc = new Locale("nb", "NO");
        }
        return navMessageSource.getMessage(kode.replace("_", "."), null, loc);
    }

    public void setTvingNorsk(boolean tvingNorsk) {
        this.tvingNorsk = tvingNorsk;
    }

}

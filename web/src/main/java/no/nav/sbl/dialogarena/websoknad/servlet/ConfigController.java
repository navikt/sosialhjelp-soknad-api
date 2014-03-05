package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Klassen håndterer rest kall for å hente config fra EnvConfig/properties
 */
@Controller
@ControllerAdvice()
public class ConfigController {

    @Inject
    ConfigService configService;
    @Inject
    LagringsScheduler lagringsScheduler;

    @RequestMapping(value = "/getConfig", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Map<String, String> getConfig() {
        return configService.getConfig();
    }

    @RequestMapping(value = "/getConfig/{soknadId}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public Map<String, String> getConfigForSoknad(@PathVariable Long soknadId) {
        return configService.getConfig(soknadId);
    }

    @RequestMapping(value="/internal/lagre")
    public void kjorLagring() throws InterruptedException {
        lagringsScheduler.mellomlagreSoknaderOgNullstillLokalDb();
    }
}
package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.LagringsScheduler;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.websoknad.config.ContentConfig;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    private ConfigService configService;
    @Inject
    private LagringsScheduler lagringsScheduler;
    @Inject
    private CacheManager cacheManager;
    @Inject
    private NavMessageSource messageSource;
    @Inject
    private ContentConfig contentConfig;

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

    @RequestMapping(value="/internal/reset")
    @ResponseBody
    public String resetCache(@RequestParam("type") String type){
        if("cms".equals(type)){
            cacheManager.getCache("cms.content").clear();
            cacheManager.getCache("cms.article").clear();
            contentConfig.lastInnNyeInnholdstekster();
            messageSource.clearCache();
            return "CACHE RESET OK";
        }
        return "OK";
    }
}
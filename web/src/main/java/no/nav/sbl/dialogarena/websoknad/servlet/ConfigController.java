package no.nav.sbl.dialogarena.websoknad.servlet;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Klassen håndterer rest kall for å hente config fra EnvConfig/properties
 * 
 */
@Controller
@ControllerAdvice()
public class ConfigController {
    Logger log = LoggerFactory.getLogger(ConfigController.class);

    @Inject
    ConfigService configService;
    
    @RequestMapping(value = "/getConfig", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
        @ResponseBody()
        public Map<String,String> sendEpost(HttpServletRequest request) {
           return configService.getConfig();
        }
}
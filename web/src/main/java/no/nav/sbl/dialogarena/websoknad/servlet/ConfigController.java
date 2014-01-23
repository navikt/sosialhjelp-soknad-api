package no.nav.sbl.dialogarena.websoknad.servlet;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Klassen håndterer rest kall for å hente config fra EnvConfig/properties
 * 
 */
@Controller
public class ConfigController {
    Logger log = LoggerFactory.getLogger(ConfigController.class);

    @RequestMapping(value = "/getConfig", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
        @ResponseBody()
        public Map<String,String> sendEpost(HttpServletRequest request) {
           Map<String, String> result = new HashMap<String, String>();
            
           
            
            return result;
        }
}
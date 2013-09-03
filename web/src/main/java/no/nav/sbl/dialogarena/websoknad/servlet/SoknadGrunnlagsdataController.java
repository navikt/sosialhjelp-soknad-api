package no.nav.sbl.dialogarena.websoknad.servlet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Klasse som henter ut grunnlagsdata for en søknad
 */
@Controller
@RequestMapping(value = "/grunnlagsdata/{nokkel}", produces = "application/json")
public class SoknadGrunnlagsdataController {
    @RequestMapping(method = RequestMethod.GET)
    public Object hentGrunnlagsdata(@PathVariable String nokkel) {
        return "";
    }
}

package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.websoknad.domain.Person;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Klasse som henter ut grunnlagsdata for en søknad
 */
@Controller
@RequestMapping("/grunnlagsdata/{nokkel}")
public class SoknadGrunnlagsdataController {
    
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentGrunnlagsdata(@PathVariable Long nokkel) {
        return Person.create();
    }
}

package no.nav.sbl.dialogarena.websoknad.servlet;

import no.aetat.arena.personstatus.Personstatus;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoConnector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@ControllerAdvice()
@RequestMapping(value = "/personinfo/{fnr}")
public class PersonInfoController {

    @Inject
    PersonInfoConnector personInfoConnector;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Personstatus hentSoknadData(@PathVariable String fnr) {
        return personInfoConnector.hent(fnr);
    }

}

package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.websoknad.domain.Person;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Klasse som henter ut grunnlagsdata for en søknad
 */
@Controller
@RequestMapping("/grunnlagsdata")
public class SoknadGrunnlagsdataController {
    
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentGrunnlagsdata() {
        Person person = Person.create();
        person.setFnr(SubjectHandler.getSubjectHandler().getUid());
        return person;
    }
}

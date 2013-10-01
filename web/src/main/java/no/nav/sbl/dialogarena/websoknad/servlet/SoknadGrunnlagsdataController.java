package no.nav.sbl.dialogarena.websoknad.servlet;

import javax.inject.Inject;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.person.Person;
import no.nav.sbl.dialogarena.person.PersonService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Klasse som henter ut grunnlagsdata for en søknad
 */
@Controller
@RequestMapping("soknad/")
public class SoknadGrunnlagsdataController {

	@Inject
	PersonService personService;

	Logger log = LoggerFactory.getLogger(SoknadGrunnlagsdataController.class);

	@RequestMapping(value = "/{soknadId}/grunnlagsdata/personalia", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentGrunnlagsdata(@PathVariable Long soknadId) {
		Person person = null;
		try {
			person = personService.hentPerson(soknadId, SubjectHandler.getSubjectHandler().getUid());
		} catch (Exception e) {
			log.info("Klarte ikke hente person.");
			person = new Person();
		}

        return person;
    }
}

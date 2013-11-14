package no.nav.sbl.dialogarena.websoknad.servlet;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import no.nav.sbl.dialogarena.person.Person;
import no.nav.sbl.dialogarena.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UtslagskriterierController {

	@Inject
	private PersonService personService;
	
    private Map<String, Boolean> utslagskriterierResultat = new HashMap<>();

    @RequestMapping(value = "utslagskriterier", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, Boolean> sjekkUtslagskriterier() {
    	String uid = getSubjectHandler().getUid();
		PersonAlder alder = new PersonAlder(uid);
    	utslagskriterierResultat.put("gyldigAlder", alder.sjekkAlder());
        utslagskriterierResultat.put("bosattINorge", harNorskAdresse(uid));
        return utslagskriterierResultat;
    }

	private Boolean harNorskAdresse(String uid) {
		Person person = personService.hentPerson(1l, uid);
		return !person.harUtenlandskAdresse();
	}

 }
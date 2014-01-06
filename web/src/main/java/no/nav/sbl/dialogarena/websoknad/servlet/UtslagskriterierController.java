package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

@Controller
public class UtslagskriterierController {

	@Inject
	private PersonService personService;
	
    private Map<String, Boolean> utslagskriterierResultat = new HashMap<>();

    @RequestMapping(value = "utslagskriterier", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, Boolean> sjekkUtslagskriterier() {
    	String uid = getSubjectHandler().getUid();
        utslagskriterierResultat.put("gyldigAlder", new PersonAlder(uid).sjekkAlder());
        utslagskriterierResultat.put("bosattINorge", harNorskAdresse(uid));
        return utslagskriterierResultat;
    }

	private Boolean harNorskAdresse(String uid) {
        return !personService.hentPerson(1l, uid).harUtenlandskAdresse();
	}

 }
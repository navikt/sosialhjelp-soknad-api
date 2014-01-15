package no.nav.sbl.dialogarena.websoknad.servlet;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Person;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UtslagskriterierController {

    @Inject
    private PersonService personService;

    private Map<String, String> utslagskriterierResultat = new HashMap<>();

    @RequestMapping(value = "utslagskriterier", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, String> sjekkUtslagskriterier() {
        String uid = getSubjectHandler().getUid();
       
        utslagskriterierResultat.put("gyldigAlder", new PersonAlder(uid).sjekkAlder().toString());
        
        Person person = personService.hentPerson(1l, uid);
        utslagskriterierResultat.put("bosattINorge", harNorskAdresse(person).toString());
        
        utslagskriterierResultat.put("registrertAdresse", person.hentGjeldendeAdresse());
        return utslagskriterierResultat;
    }

    private Boolean harNorskAdresse(Person person) {
        return person.harUtenlandskAdresse();
    }

}
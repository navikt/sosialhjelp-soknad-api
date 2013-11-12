package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.person.Person;
import no.nav.sbl.dialogarena.person.PersonService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

@Controller
@RequestMapping("/soknad")
public class SoknadTpsDataController {
	
	@Inject
    private Kodeverk kodeverk;
	
	@Inject
	private PersonService personService;
	
    @RequestMapping(value = "/kodeverk/{postnummer}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public String hentPoststed(@PathVariable String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }
    
    @RequestMapping(value = "/kodeverk/landliste", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, List<String>> hentLandkodeListe() {
        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
        List<String> mockLand = asList(
                "Norge",
                "Sverige",
                "Danmark"
        );
        hashMap.put("result", mockLand);
    	return hashMap;
    }
	
    @RequestMapping(value = "/{soknadId}/personalia", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentPerson(@PathVariable String soknadId) {
        return personService.hentPerson(new Long(soknadId), getSubjectHandler().getUid());
    }
    
    @RequestMapping(value = "/{soknadId}/personalia/fnr/{fnr}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentEnPerson(@PathVariable String soknadId, @PathVariable String fnr) {
    	return personService.hentPerson(new Long(soknadId), fnr);
    }

}

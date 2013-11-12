package no.nav.sbl.dialogarena.websoknad.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.person.Adresse;
import no.nav.sbl.dialogarena.person.Person;
import no.nav.sbl.dialogarena.person.PersonService;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.service.SendSoknadService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

@Controller
@RequestMapping("/soknad")
public class SoknadTpsDataController {

	@Inject
    private SendSoknadService soknadService;
	
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
        ArrayList<String> mockLand = new ArrayList<>();
        
        mockLand.add("Norge");
        mockLand.add("Sverige");
        mockLand.add("Danmark");
        
        
        hashMap.put("result", mockLand);
    	return hashMap;
    }
	
    @RequestMapping(value = "/{soknadId}/personalia", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentPerson(@PathVariable String soknadId) {
        String fnr = SubjectHandler.getSubjectHandler().getUid();
        
    	Person person = personService.hentPerson(new Long(soknadId), fnr);
    
    	for (Object faktumObj : person.getFakta().values()) {
    		if(faktumObj instanceof Faktum) {
    			Faktum faktum = (Faktum) faktumObj;
    			soknadService.lagreSoknadsFelt(new Long(soknadId), faktum.getKey(), faktum.getValue());
    		} else if (faktumObj instanceof List<?>) {
    			@SuppressWarnings("unchecked")
				List<Adresse> adresseList = (List<Adresse>) faktumObj;
    			String adresseJson = new Gson().toJson(adresseList);
    			soknadService.lagreSoknadsFelt(new Long(soknadId), "adresser", adresseJson);
    		}
    	}
        
    	return person;
    }
    
    @RequestMapping(value = "/{soknadId}/personalia/fnr/{fnr}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentEnPerson(@PathVariable String soknadId, @PathVariable String fnr) {
  
    	return personService.hentPerson(new Long(soknadId), fnr);
    }
}

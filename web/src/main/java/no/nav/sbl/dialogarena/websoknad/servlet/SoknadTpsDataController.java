package no.nav.sbl.dialogarena.websoknad.servlet;

import com.google.gson.Gson;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.person.Adresse;
import no.nav.sbl.dialogarena.person.Person;
import no.nav.sbl.dialogarena.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;

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
    public Map<String, List<Map<String, String>>> hentLandkodeListe() {
        Map<String, String> norge = new LinkedHashMap<>();
        norge.put("text", "Norge");
        norge.put("value", "NO");

        Map<String, String> sverige = new LinkedHashMap<>();
        sverige.put("text", "Sverige");
        sverige.put("value", "SE");

        Map<String, String> danmark = new LinkedHashMap<>();
        danmark.put("text", "Danmark");
        danmark.put("value", "DK");

        List<Map<String, String>> mockLand = new ArrayList<>();
        mockLand.add(norge);
        mockLand.add(sverige);
        mockLand.add(danmark);

        Map<String, List<Map<String, String>>> hashMap = new LinkedHashMap<>();
        hashMap.put("result", mockLand);
    	return hashMap;
    }
	
    @RequestMapping(value = "/{soknadId}/personalia", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentPerson(@PathVariable String soknadId) {
        Person person = personService.hentPerson(new Long(soknadId), getSubjectHandler().getUid());
    
    	for (Object faktumObj : person.getFakta().values()) {
    		if (faktumObj instanceof Faktum) {
    			Faktum faktum = (Faktum) faktumObj;
    			soknadService.lagreSystemSoknadsFelt(new Long(soknadId), faktum.getKey(), faktum.getValue());
    		} else if (faktumObj instanceof List<?>) {
    			@SuppressWarnings("unchecked")
				List<Adresse> adresseList = (List<Adresse>) faktumObj;
    			String adresseJson = new Gson().toJson(adresseList);
    			soknadService.lagreSystemSoknadsFelt(new Long(soknadId), "adresser", adresseJson);
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

package no.nav.sbl.dialogarena.websoknad.servlet;

import com.google.gson.GsonBuilder;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adresse;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.FamilieRelasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Person;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.websoknad.util.DateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    @Inject
    private FamilieRelasjonService familieRelasjonService;

    @RequestMapping(value = "/kodeverk/{postnummer}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public String hentPoststed(@PathVariable String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }

    @RequestMapping(value = "/kodeverk/landliste", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, List<Map<String, String>>> hentLandkodeListe() {

        List<Map<String, String>> landliste = new ArrayList<>();
        List<String> landKoder = kodeverk.getAlleLandkoder();
        for (String landkode : landKoder) {
            Map<String, String> land = new LinkedHashMap<>();
            land.put("text", kodeverk.getLand(landkode));
            land.put("value", landkode);
            landliste.add(land);
        }
        if (!landKoder.contains("NOR")) {
            Map<String, String> norge = new LinkedHashMap<>();
            norge.put("text", "Norge");
            norge.put("value", "NOR");
            landliste.add(norge);
        }
        Map<String, List<Map<String, String>>> resultMap = new LinkedHashMap<>();
        resultMap.put("result", landliste);
        return resultMap;
    }

    @RequestMapping(value = "/personalder", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, Integer> getAlder() {
        Map<String, Integer> result = new HashMap<>();
        String uid = getSubjectHandler().getUid();
        PersonAlder personAlder = new PersonAlder(uid);

        result.put("alder", personAlder.getAlder());
        return result;
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

                GsonBuilder gson = new GsonBuilder();
                gson.registerTypeAdapter(DateTime.class, new DateTimeSerializer());

                String adresseJson = gson.create().toJson(adresseList);

                soknadService.lagreSystemSoknadsFelt(new Long(soknadId), "adresser", adresseJson);
            }
        }

        // TODO: Fjern en gang!
        List<Adresse> adresser = new ArrayList<>();
        Adresse ad1 = new Adresse(new Long(soknadId), Adressetype.BOSTEDSADRESSE);
        ad1.setGatenavn("Gata");
        ad1.setHusnummer("1");
        ad1.setHusbokstav("B");
        ad1.setPostnummer("0001");
        ad1.setPoststed("Plassen");
        adresser.add(ad1);

        Adresse ad2 = new Adresse(new Long(soknadId), Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE);
        ad2.setGatenavn("Gata");
        ad2.setHusnummer("1");
        ad2.setHusbokstav("B");
        ad2.setPostnummer("0001");
        ad2.setPoststed("Plassen");
        ad2.setGyldigtil(DateTime.parse("2015-01-01"));

        adresser.add(ad1);
        adresser.add(ad2);
        Person MockPerson = new Person(new Long(soknadId), "01015245464", "Bob", "Kåre", "Byggmester", Adressetype.BOSTEDSADRESSE.toString(), adresser);
        return MockPerson;
    }

    @RequestMapping(value = "/{soknadId}/familierelasjoner", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentFamilierelasjoner(@PathVariable String soknadId) {
        return familieRelasjonService.hentPerson(new Long(soknadId), getSubjectHandler().getUid());
    }


    @RequestMapping(value = "/{soknadId}/personalia/fnr/{fnr}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Person hentEnPerson(@PathVariable String soknadId, @PathVariable String fnr) {
        return personService.hentPerson(new Long(soknadId), fnr);
    }

}

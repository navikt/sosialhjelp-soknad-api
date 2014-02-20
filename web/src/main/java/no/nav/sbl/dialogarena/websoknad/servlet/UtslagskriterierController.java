package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoConnector;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
public class UtslagskriterierController {
    private static final Logger logger = getLogger(UtslagskriterierController.class);
    @Inject
    private PersonaliaService personaliaService;

    @Inject
    PersonInfoConnector personInfoConnector;

    private Map<String, String> utslagskriterierResultat = new HashMap<>();

    @RequestMapping(value = "utslagskriterier", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, String> sjekkUtslagskriterier() {
        String uid = getSubjectHandler().getUid();
        PersonInfoConnector.Status status = personInfoConnector.hentArbeidssokerStatus(uid);
        utslagskriterierResultat.put("registrertArbeidssøker", status.name());

        Personalia personalia = null;

        try
        {
               personalia = personaliaService.hentPersonalia(uid);
        }
        catch (Exception e)
        {
            logger.error("Kunne ikke hente personalia" + e.getMessage());
        }
            utslagskriterierResultat.put("gyldigAlder", new PersonAlder(uid).sjekkAlder().toString());
            utslagskriterierResultat.put("bosattINorge", harNorskAdresse(personalia).toString());
            utslagskriterierResultat.put("registrertAdresse", personalia.getGjeldendeAdresse().getAdresse());
            utslagskriterierResultat.put("registrertAdresseGyldigFra", personalia.getGjeldendeAdresse().getGyldigFra());
            utslagskriterierResultat.put("registrertAdresseGyldigTil", personalia.getGjeldendeAdresse().getGyldigTil());
        
        return utslagskriterierResultat;
        
    }

    private Boolean harNorskAdresse(Personalia personalia) {
        return !personalia.harUtenlandskAdresse();
    }

}
package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
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
    private PersonaliaService personaliaService;

    private Map<String, String> utslagskriterierResultat = new HashMap<>();

    @RequestMapping(value = "utslagskriterier", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, String> sjekkUtslagskriterier() {
        String uid = getSubjectHandler().getUid();
        Personalia personalia = personaliaService.hentPersonalia(uid);
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
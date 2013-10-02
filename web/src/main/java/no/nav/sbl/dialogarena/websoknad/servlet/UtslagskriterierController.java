package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.websoknad.domain.PersonAlder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/utslagskriterier")
public class UtslagskriterierController {

    private Map<String, Boolean> utslagskriterierResultat = new HashMap<>();

    @RequestMapping(value = "/{uid}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, Boolean> sjekkUtslagskriterier() {
        utslagskriterierResultat.put("alder", sjekkAlder());
        utslagskriterierResultat.put("borIUtland", true);
        return utslagskriterierResultat;
    }

    private boolean sjekkAlder() {
        PersonAlder alder = new PersonAlder(SubjectHandler.getSubjectHandler().getUid());
        return alder.getAlder() < 67;
    }
 }





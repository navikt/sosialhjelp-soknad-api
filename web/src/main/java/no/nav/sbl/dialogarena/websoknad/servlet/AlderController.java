package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.websoknad.domain.PersonAlder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created with IntelliJ IDEA.
 * User: I140481
 * Date: 01.10.13
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/grunnlagsdata/alder")

public class AlderController {
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public PersonAlder hentAlder() {
        PersonAlder alder = new PersonAlder(SubjectHandler.getSubjectHandler().getUid());
        return alder;
    }
 }





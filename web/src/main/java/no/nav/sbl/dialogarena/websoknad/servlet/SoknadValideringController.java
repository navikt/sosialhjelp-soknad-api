package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Klassen håndterer innsending av en søknad.
 * Tar i mot en form, og forwarder tilbake til en annen side
 */
@Controller
@RequestMapping(value = "/valider/{soknadId}", produces = "application/json")
public class SoknadValideringController {

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ValideringsResultat validerFelt(@PathVariable Long soknadId, @RequestBody Faktum faktum) {
        return new ValideringsResultat();
    }
}

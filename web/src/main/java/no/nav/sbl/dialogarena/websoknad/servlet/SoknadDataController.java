package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Controller
@RequestMapping("/soknad/{soknadId}")
public class SoknadDataController {

    @Inject
    private WebSoknadService soknadService;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public WebSoknad hentSoknadData(@PathVariable Long soknadId) {
        return soknadService.hentSoknad(soknadId);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody()
    public void lagreSoknad(@PathVariable Long soknadId, @RequestBody WebSoknad webSoknad) {
        for (Faktum faktum : webSoknad.getFakta().values()) {
            soknadService.lagreSoknadsFelt(soknadId, faktum.getKey(), faktum.getValue());
        }
    }

    @RequestMapping(value = "/{faktum}", method = RequestMethod.POST)
    public void lagreFaktum(@PathVariable Long soknadId, @PathVariable Long faktumId, @RequestBody Faktum faktum) {
        if(!faktumId.equals(faktum.getKey())){
            throw new ApplicationException("Ikke samsvarende faktuimId");
        }
        soknadService.lagreSoknadsFelt(soknadId, faktum.getKey(), faktum.getValue());
    }

    @RequestMapping(value = "/{faktum}", method = RequestMethod.GET)
    public void hentFaktum(@PathVariable Long soknadId, @PathVariable Long faktumId) {
        throw new ApplicationException("Ikke implementert enda. ");
    }
    
    
}

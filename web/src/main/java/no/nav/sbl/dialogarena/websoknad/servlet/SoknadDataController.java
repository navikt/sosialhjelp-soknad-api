package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknadId;
import no.nav.sbl.dialogarena.websoknad.service.SendSoknadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Controller
@RequestMapping("/soknad")
public class SoknadDataController {

    @Inject
    private SendSoknadService soknadService;

    @RequestMapping(value = "/{soknadId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public WebSoknad hentSoknadData(@PathVariable Long soknadId) {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        return soknad;
    }

    @RequestMapping(value = "/options/{soknadId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public SoknadStruktur hentSoknadStruktur(@PathVariable Long soknadId) {

        WebSoknad webSoknad = soknadService.hentSoknad(soknadId);
        String type = webSoknad.getGosysId() + ".xml";
        try {
            JAXBContext context = JAXBContext.newInstance(SoknadStruktur.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class.getResourceAsStream(String.format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }

    @RequestMapping(value = "/send/{soknadId}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody()
    public void sendSoknad(@PathVariable Long soknadId) {
        soknadService.sendSoknad(soknadId);
    }

    @RequestMapping(value = "/lagre/{soknadId}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody()
    public void lagreSoknad(@PathVariable Long soknadId, @RequestBody WebSoknad webSoknad) {
        for (Faktum faktum : webSoknad.getFakta().values()) {
            soknadService.lagreSoknadsFelt(soknadId, faktum.getKey(), faktum.getValue());
        }
    }

    @RequestMapping(value = "/opprett/{soknadType}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody()
    public WebSoknadId opprettSoknad(@PathVariable String soknadType) {
        Long id = soknadService.startSoknad(soknadType);

        WebSoknadId soknadId = new WebSoknadId();
        soknadId.setId(id);
        return soknadId;
    }

    @RequestMapping(value = "/delete/{soknadId}", method = RequestMethod.POST)
    @ResponseBody()
    public void slettSoknad(@PathVariable Long soknadId) {
        soknadService.avbrytSoknad(soknadId);
    }

//    @RequestMapping(value = "/{soknadId}/{faktum}", method = RequestMethod.POST)
//    public void lagreFaktum(@PathVariable Long soknadId, @PathVariable Long faktumId, @RequestBody Faktum faktum) {
//        if(!faktumId.equals(faktum.getKey())){
//            throw new ApplicationException("Ikke samsvarende faktuimId");
//        }
//        soknadService.lagreSoknadsFelt(soknadId, faktum.getKey(), faktum.getValue());
//    }
//
//    @RequestMapping(value = "/{soknadId}/{faktum}", method = RequestMethod.GET)
//    public void hentFaktum(@PathVariable Long soknadId, @PathVariable Long faktumId) {
//        throw new ApplicationException("Ikke implementert enda. ");
//    }
}

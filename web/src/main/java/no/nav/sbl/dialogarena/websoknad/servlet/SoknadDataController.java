package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknadId;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.websoknad.service.HenvendelseConnector;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Controller
@RequestMapping("/soknad")
public class SoknadDataController {

    @Inject
    private SoknadService soknadService;
        
    @Inject
    @Named("henvendelseConnector")
    private HenvendelseConnector henvendelseConnector;

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
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class).createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class.getResourceAsStream(format("/soknader/%s", type)));
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
        //String behandlingsId = henvendelseConnector.startSoknad(SubjectHandler.getSubjectHandler().getUid(), null);
        Long id = soknadService.startSoknad(soknadType);
        WebSoknadId soknadId = new WebSoknadId();
        soknadId.setId(id);
        return soknadId;
    }

    @RequestMapping(value = "/delete/{soknadId}", method = RequestMethod.POST)
    @ResponseBody()
    public void slettSoknad(@PathVariable Long soknadId) {
        soknadService.avbrytSoknad(soknadId);
        henvendelseConnector.avbrytSoknad("12412412");
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

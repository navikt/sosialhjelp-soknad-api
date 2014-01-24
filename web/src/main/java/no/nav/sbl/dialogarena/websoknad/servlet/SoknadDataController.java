package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.print.HandleBarKjoerer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.VedleggForventning;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.apache.commons.collections15.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.modig.lang.collections.IterUtils.on;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Controller
@ControllerAdvice()
@RequestMapping("/soknad")
public class SoknadDataController {

    @Inject
    private SendSoknadService soknadService;
    @Inject
    private VedleggService vedleggService;

    @Inject
    private Kodeverk kodeverk;

    @RequestMapping(value = "/{soknadId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public WebSoknad hentSoknadData(@PathVariable Long soknadId) {
        return soknadService.hentSoknad(soknadId);
    }
    
    @RequestMapping(value = "/behandling/{behandlingsId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String,String> hentSoknadIdMedBehandligsId(@PathVariable String behandlingsId) {
        Map<String, String> result = new HashMap<>();
        String soknadId = soknadService.hentSoknadMedBehandlinsId(behandlingsId).toString();
        result.put("result", soknadId);
        
        return result;
    }

    @RequestMapping(value = "/options/{soknadId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public SoknadStruktur hentSoknadStruktur(@PathVariable Long soknadId) {
        String type = soknadService.hentSoknad(soknadId).getskjemaNummer() + ".xml";
        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }



    @RequestMapping(value = "{soknadId}/{faktumId}/forventning", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public List<Vedlegg> hentPaakrevdeVedleggForFaktum(
            @PathVariable final Long soknadId, @PathVariable final Long faktumId) {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        return on(vedleggService.hentPaakrevdeVedlegg(soknadId, soknad)).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedleggForventning) {
                return vedleggForventning.getFaktumId().equals(faktumId);
            }
        }).collect();
    }

    @RequestMapping(value = "{soknadId}/forventning/valg", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @ResponseStatus(HttpStatus.OK)
    public void endreValg(@PathVariable final Long soknadId,
                          @RequestBody VedleggForventning forventning) {
        soknadService.endreInnsendingsvalg(soknadId, forventning.getFaktum());
    }

    @RequestMapping(value = "/send/{soknadId}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody()
    public void sendSoknad(@PathVariable Long soknadId) {
        soknadService.sendSoknad(soknadId);
    }

    @RequestMapping(value = "/lagre/{soknadId}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody()
    public void lagreSoknad(@PathVariable Long soknadId,
                            @RequestBody WebSoknad webSoknad) {
        for (Faktum faktum : webSoknad.getFakta().values()) {
            soknadService.lagreSoknadsFelt(soknadId, faktum);
        }
    }

    @RequestMapping(value = "/{soknadId}/faktum/", method = RequestMethod.POST)
    @ResponseBody()
    public Faktum lagreFaktum(@PathVariable Long soknadId,
                              @RequestBody Faktum faktum) {
        return soknadService.lagreSoknadsFelt(soknadId, faktum);
    }

    @RequestMapping(value = "/opprett/{soknadType}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody()
    public Map<String,String> opprettSoknad(@PathVariable String soknadType) {
        Map<String, String> result = new HashMap<>();
        
        String behandlingId = soknadService.startSoknad(soknadType);
        result.put("brukerbehandlingId", behandlingId);
        
        return result;
    }

    @RequestMapping(value = "/delete/{soknadId}", method = RequestMethod.POST)
    @ResponseBody()
    public void slettSoknad(@PathVariable Long soknadId) {
        soknadService.avbrytSoknad(soknadId);
        // Må legges til i forbindelse med kobling mot henvendelse.
        // henvendelseConnector.avbrytSoknad("12412412");
    }

    @RequestMapping(value = "/oppsummering/{soknadId}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody()
    public String hentOppsummering(@PathVariable Long soknadId) throws IOException {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);

        String markup = new HandleBarKjoerer(kodeverk).fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger");
        return markup;
    }
}

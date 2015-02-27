package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
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
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Controller
@ControllerAdvice()
@RequestMapping(value = "/soknad/{soknadId}/fakta")
public class FaktaController {

    @Inject
    private FaktaService faktaService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public List<Faktum> hentSoknadData(@PathVariable Long soknadId) {
        return faktaService.hentFakta(soknadId);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public Faktum lagreNyttFaktum(@PathVariable Long soknadId, @RequestBody Faktum faktum) {
        return faktaService.lagreSoknadsFelt(soknadId, faktum);
    }

    @RequestMapping(value = "/{faktumId}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public Faktum lagreFaktum(@PathVariable Long soknadId, @RequestBody Faktum faktum) {
        return faktaService.lagreSoknadsFelt(soknadId, faktum);
    }

    @RequestMapping(value = "/{faktumId}/delete", method = RequestMethod.POST)
    @ResponseBody()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SjekkTilgangTilSoknad
    public void slettFaktum(@PathVariable Long soknadId, @PathVariable Long faktumId) {
        faktaService.slettBrukerFaktum(soknadId, faktumId);
    }

}

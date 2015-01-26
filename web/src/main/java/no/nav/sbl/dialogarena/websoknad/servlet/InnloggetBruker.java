package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class InnloggetBruker {

    @Inject
    private PersonaliaService personaliaService;

    private static final Logger logger = getLogger(InnloggetBruker.class);

    @RequestMapping(value = "/personalia/{soknadId}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody
    @SjekkTilgangTilSoknad
    public void lagrePersonaliaOgBarn(@PathVariable Long soknadId, @RequestBody(required = false) Boolean lagreBarn) {
        String fnr = SubjectHandler.getSubjectHandler().getUid();
        Boolean skalLagreBarn = lagreBarn;
        if (skalLagreBarn == null) {
            skalLagreBarn = false;
        }
        personaliaService.lagrePersonaliaOgBarn(fnr, soknadId, skalLagreBarn);
    }

    public Personalia hentPersonalia() {
        String fnr = SubjectHandler.getSubjectHandler().getUid();
        Personalia personalia = null;
        try {
            personalia = personaliaService.hentPersonalia(fnr);
        } catch (Exception e) {
            logger.error("Kunne ikke hente personalia");
        }
        return personalia;
    }
}

package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.websoknad.domain.FortsettSenere;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static no.nav.sbl.dialogarena.websoknad.servlet.ServerUtils.getGjenopptaUrl;
import static org.apache.wicket.model.Model.of;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Klassen håndterer rest kall for å sende epost for at brukeren kan fortsette søknaden senere.
 */
@Controller
@RequestMapping("/soknad")
public class FortsettSenereController {

    Logger log = LoggerFactory.getLogger(FortsettSenereController.class);
    @Inject
    private EmailService emailService;

    @RequestMapping(value = "/{behandlingId}/fortsettsenere", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public void sendEpost(HttpServletRequest request, @PathVariable String behandlingId, @RequestBody FortsettSenere epost) {
        ValueMap map = new ValueMap();
        map.put("url", getGjenopptaUrl(request.getRequestURL().toString(), behandlingId));
        String content = new StringResourceModel("fortsettSenere.sendEpost.epostInnhold", of(map)).getString();
        emailService.sendFortsettSenereEPost(epost.getEpost(), "Lenke til påbegynt dagpengesøknad", content);
    }
}

package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.websoknad.domain.FortsettSenere;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static no.nav.sbl.dialogarena.websoknad.servlet.ServerUtils.getGjenopptaUrl;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Klassen håndterer rest kall for å sende epost for at brukeren kan fortsette søknaden senere.
 */
@Controller
@RequestMapping("/soknad")
public class FortsettSenereController {

    @Inject
    private EmailService emailService;

    @Inject
    @Named("navMessageSource")
    private MessageSource messageSource;

    @RequestMapping(value = "/{soknadId}/{behandlingId}/fortsettsenere", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public void sendEpost(HttpServletRequest request, @PathVariable String soknadId, @PathVariable String behandlingId, @RequestBody FortsettSenere epost) {
        String content = messageSource.getMessage("fortsettSenere.sendEpost.epostInnhold",
                new Object[]{getGjenopptaUrl(request.getRequestURL().toString(), soknadId, behandlingId)}, new Locale("nb", "NO"));
        emailService.sendFortsettSenereEPost(epost.getEpost(), "Lenke til påbegynt dagpengesøknad", content);
    }
}

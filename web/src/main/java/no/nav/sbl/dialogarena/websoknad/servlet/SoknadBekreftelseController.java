package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.EmailService;
import no.nav.sbl.dialogarena.websoknad.domain.FortsettSenere;
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

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.ServerUtils.getEttersendelseUrl;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * Klassen håndterer restkall for å sende epost etter at søknaden er sendt inn.
 */
@Controller
@RequestMapping("/bekreftelse")
public class SoknadBekreftelseController {

    @Inject
    private EmailService emailService;

    @Inject
    @Named("navMessageSource")
    private MessageSource messageSource;

    @RequestMapping(value = "/{behandlingId}/{bekreftelsesmail}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public void sendEpost(HttpServletRequest request, @PathVariable String behandlingId, String bekreftelsesmail) {
       String content = "sdfds";// messageSource.getMessage("fortsettSenere.sendEpost.epostInnhold", new Object[]{getEttersendelseUrl(request.getRequestURL().toString(), soknadId, behandlingId)}, new Locale("nb", "NO"));
//        emailService.sendFortsettSenereEPost(epost.getEpost(), "Lenke til påbegynt dagpengesøknad", content);
    }
}


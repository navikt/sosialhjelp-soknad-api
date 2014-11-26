package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.ConfigService;
import no.nav.sbl.dialogarena.websoknad.service.EmailService;
import no.nav.sbl.dialogarena.websoknad.domain.SoknadBekreftelse;
import org.slf4j.Logger;
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

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * Klassen håndterer restkall for å sende epost etter at søknaden er sendt inn.
 */
@Controller
@RequestMapping("/bekreftelse")
public class SoknadBekreftelseController {
    private static final Logger logger = getLogger(SoknadBekreftelseController.class);

    @Inject
    private EmailService emailService;

    @Inject
    @Named("navMessageSource")
    private MessageSource messageSource;

    @Inject
    private ConfigService configService;

    @RequestMapping(value = "/{behandlingId}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public void sendEpost(HttpServletRequest request, @PathVariable String behandlingId, @RequestBody SoknadBekreftelse soknadBekreftelse) {
        if (soknadBekreftelse.getEpost() != null) {
            String subject = messageSource.getMessage("sendtSoknad.sendEpost.epostSubject", null, new Locale("nb", "NO"));
            String saksoversiktUrl = configService.getValue("saksoversikt.link.url") + "/detaljer/"+ soknadBekreftelse.getTemaKode() +"/" + behandlingId;
            String ettersendelseUrl = ServerUtils.getEttersendelseUrl(request.getRequestURL().toString(), behandlingId);

            String innhold = messageSource.getMessage("sendtSoknad.sendEpost.epostInnhold", new Object[]{saksoversiktUrl, ettersendelseUrl}, new Locale("nb", "NO"));

            if(soknadBekreftelse.getErEttersendelse()) {
                innhold = messageSource.getMessage("sendEttersendelse.sendEpost.epostInnhold", new Object[]{saksoversiktUrl}, new Locale("nb", "NO"));
            }

            emailService.sendEpostEtterInnsendtSoknad(soknadBekreftelse.getEpost(), subject, innhold, behandlingId);

        } else {
            logger.debug("Fant ingen epost");
        }
    }
}


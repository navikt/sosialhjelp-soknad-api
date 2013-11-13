package no.nav.sbl.dialogarena.websoknad.servlet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import no.nav.sbl.dialogarena.websoknad.service.EmailService;

import org.apache.wicket.model.Model;
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

/**
 * Klassen håndterer rest kall for å sende epost for at brukeren kan fortsette søknaden senere.
 */
@Controller
@RequestMapping("/soknad")
public class FortsettSenereController {

	Logger log = LoggerFactory.getLogger(FortsettSenereController.class);
	
	@Inject
	private EmailService emailService;

	    @RequestMapping(value = "/{soknadId}/fortsettsenere", method = RequestMethod.POST)
	    @ResponseBody()
	    public void sendEpost(HttpServletRequest request, @PathVariable Long soknadId, @RequestBody String epost) {
	        String url = ServerUtils.getGjenopptaUrl(request.getRequestURL().toString(), soknadId);
	        ValueMap map = new ValueMap();
	        map.put("url", url);
	        String content = new StringResourceModel("fortsettSenere.sendEpost.epostInnhold", Model.of(map)).getString();
	    	emailService.sendFortsettSenereEPost(epost, "Lenke til påbegynt dagpengesøknad", content);
	    }
}

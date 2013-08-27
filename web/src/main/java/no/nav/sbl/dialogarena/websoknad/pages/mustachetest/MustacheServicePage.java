package no.nav.sbl.dialogarena.websoknad.pages.mustachetest;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;



import org.apache.wicket.markup.html.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MustacheServicePage extends WebPage{
	@Inject
	private WebSoknadService soknadService;


	Logger log = LoggerFactory.getLogger(MustacheServicePage.class);
	
	public MustacheServicePage() {
		HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
		HttpServletResponse response = (HttpServletResponse) getResponse().getContainerResponse();
		
		if(request.getMethod().equalsIgnoreCase("GET")) {
			Long soknadId = soknadService.startSoknad("test");
			WebSoknad soknad = soknadService.hentSoknad(soknadId);
			response.setHeader("SoknadId", soknadId.toString());
			response.setHeader("navn", soknad.getFakta().get("fornavn").getValue() + " " + soknad.getFakta().get("etternavn").getValue());
			response.setHeader("fnr", soknad.getFakta().get("fnr").getValue());
			response.setHeader("adresse", soknad.getFakta().get("adresse").getValue());
						
		} else if(request.getMethod().equalsIgnoreCase("POST")) {
			haandterePost(request, response);
		}
	}

	private void haandterePost(HttpServletRequest request, HttpServletResponse response) {
		Long soknadId = Long.valueOf(request.getParameter("soknadId"));
		Map<String, String[]> parameterMap = request.getParameterMap();
		String resultat = "";
		for (String key : parameterMap.keySet()) {
			String value = request.getParameter(key);
			soknadService.lagreSoknadsFelt(soknadId, key, value);
			log.debug("Soknadid: " + soknadId + " key: " + key + " value: " + value);
			resultat += "<p>" + key + ": " + value + "</p>";
		}
		
		resultat +="<p><span>Jeg samtykker i at overstående informasjon er riktig</span><input type='checkbox'/><p>";
		resultat +="<p><input type='submit' value='Send inn'/><p>";
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getOutputStream().print("<h1>Oppsummering</h1>" + resultat);
		} catch (IOException e) {
			log.info("Klarte ikke skrive oppsummering.");
		}
	}

}

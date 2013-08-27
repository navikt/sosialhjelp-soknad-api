package no.nav.sbl.dialogarena.websoknad.pages.sendsoknad;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;



import org.apache.wicket.markup.html.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendSoknadServicePage extends WebPage{
	@Inject
	private WebSoknadService soknadService;


	Logger log = LoggerFactory.getLogger(SendSoknadServicePage.class);
	
	public SendSoknadServicePage() {
		HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
		HttpServletResponse response = (HttpServletResponse) getResponse().getContainerResponse();
		
		if(request.getMethod().equalsIgnoreCase("GET")) {
			Long soknadId;
			if(request.getParameter("soknadId") == null){
				soknadId = soknadService.startSoknad("test");
			}
			else{
				soknadId = Long.valueOf(request.getParameter("soknadId"));
			}
			WebSoknad soknad = soknadService.hentSoknad(soknadId);
			response.setHeader("soknadId", soknadId.toString());
			
			Map<String, Faktum> fakta = soknad.getFakta();
			for (String key : fakta.keySet()) {
				response.setHeader(key, fakta.get(key).getValue());						
			}
						
		} else if(request.getMethod().equalsIgnoreCase("POST")) {
			haandterePost(request, response);
		}
	}

	private void haandterePost(HttpServletRequest request, HttpServletResponse response) {
		Long soknadId = Long.valueOf(request.getParameter("soknadId"));
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (String key : parameterMap.keySet()) {
			String value = request.getParameter(key);
			soknadService.lagreSoknadsFelt(soknadId, key, value);
			log.debug("Soknadid: " + soknadId + " key: " + key + " value: " + value);
		}
		try {
			response.sendRedirect("oppsumering?soknadId="+soknadId);
		} catch (IOException e) {
			log.info("Klarte ikke sende redirect til oppsumeringssiden.");
		}
	}
}

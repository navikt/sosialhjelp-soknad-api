package no.nav.sbl.dialogarena.websoknad.pages.sendsoknad;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

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
			} else {
				soknadId = Long.valueOf(request.getParameter("soknadId"));
			}
			WebSoknad soknad = soknadService.hentSoknad(soknadId);
			response.setHeader("soknadId", soknadId.toString());
			
			Map<String, Faktum> fakta = soknad.getFakta();
			for (Entry<String, Faktum> entry : fakta.entrySet()) {
				response.setHeader(entry.getKey(), entry.getValue().getValue());						
			}
						
		} else if(request.getMethod().equalsIgnoreCase("POST")) {
			
			
			String bekreftetParameter = request.getParameter("bekreftet");
			if(bekreftetParameter != null && bekreftetParameter.equals("ja")) {
				haandtereKvitteringsPost(request,response);
			} else {
				haandtereOppsumeringsPost(request, response);
			}
		}
	}

	private void haandtereKvitteringsPost(HttpServletRequest request,
			HttpServletResponse response) {	
		Long soknadId = Long.valueOf(request.getParameter("soknadId"));
		soknadService.sendSoknad(soknadId);
		
		try {
			response.sendRedirect("soknadKvittering");
		} catch (IOException e) {
			log.info("Klarte ikke sende redirect til kvitteringssiden.");
		}
	}

	private void haandtereOppsumeringsPost(HttpServletRequest request, HttpServletResponse response) {
		Long soknadId = Long.valueOf(request.getParameter("soknadId"));
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Entry<String, String[]> entry : parameterMap.entrySet()) {
			String value = request.getParameter(entry.getKey());
			soknadService.lagreSoknadsFelt(soknadId, entry.getKey(), value);
			log.debug("Soknadid: " + soknadId + " key: " + entry + " value: " + value);
		}
		try {
			response.sendRedirect("oppsumering?soknadId="+soknadId);
		} catch (IOException e) {
			log.info("Klarte ikke sende redirect til oppsumeringssiden.");
		}
	}
}

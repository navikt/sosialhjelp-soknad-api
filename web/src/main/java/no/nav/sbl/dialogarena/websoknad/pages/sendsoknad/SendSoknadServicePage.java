package no.nav.sbl.dialogarena.websoknad.pages.sendsoknad;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.service.SendSoknadService;

import org.apache.wicket.markup.html.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class SendSoknadServicePage extends WebPage {
	@Inject
	private SendSoknadService soknadService;
	
	Logger log = LoggerFactory.getLogger(SendSoknadServicePage.class);

	public SendSoknadServicePage() {
		HttpServletRequest request = (HttpServletRequest) getRequest()
				.getContainerRequest();
		HttpServletResponse response = (HttpServletResponse) getResponse()
				.getContainerResponse();

		if (request.getMethod().equalsIgnoreCase("GET")) {
			Long soknadId;

			if (request.getParameter("soknadId") == null) {
				soknadId = soknadService.startSoknad("NAV 04-01.03");
			} else {
				soknadId = Long.valueOf(request.getParameter("soknadId"));
			}

			WebSoknad soknad = soknadService.hentSoknad(soknadId);
            response.setHeader("soknadId", soknadId.toString());
			response.setHeader("brukerBehandlingId", soknad.getBrukerBehandlingId().toString());

			Map<String, Faktum> fakta = soknad.getFakta();
			for (Entry<String, Faktum> entry : fakta.entrySet()) {
				response.setHeader(entry.getKey(), entry.getValue().getValue());
			}

		} else if (request.getMethod().equalsIgnoreCase("POST")) {
			String avbrytParameter = request.getParameter("avbryt");
			String bekreftetParameter = request.getParameter("bekreftet");
			if (avbrytParameter != null && avbrytParameter.equals("Avbryt")) {
				haandtereAvbrytPost(request,response);
			} else if (bekreftetParameter != null && bekreftetParameter.equals("ja")) {
				haandtereKvitteringsPost(request, response);
			} else {
				haandtereOppsumeringsPost(request, response);
			}
		}
	}

	private void haandtereAvbrytPost(HttpServletRequest request,
			HttpServletResponse response) {
		Long soknadId = Long.valueOf(request.getParameter("soknadId"));
		soknadService.avbrytSoknad(soknadId);
		try {
			response.sendRedirect("startSoknad");
		} catch (IOException e) {
			log.info("Klarte ikke sende redirect til startsiden.");
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

	private void haandtereOppsumeringsPost(HttpServletRequest request,
			HttpServletResponse response) {
		Long soknadId = Long.valueOf(request.getParameter("soknadId"));
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Entry<String, String[]> entry : parameterMap.entrySet()) {
			String value = request.getParameter(entry.getKey());
			soknadService.lagreSoknadsFelt(soknadId, entry.getKey(), value);
			log.debug("Soknadid: " + soknadId + " key: " + entry + " value: "
					+ value);
		}
		try {
			response.sendRedirect("oppsumering?soknadId=" + soknadId);
		} catch (IOException e) {
			log.info("Klarte ikke sende redirect til oppsumeringssiden.");
		}
	}
}

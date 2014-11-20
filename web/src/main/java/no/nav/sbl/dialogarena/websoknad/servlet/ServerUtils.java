package no.nav.sbl.dialogarena.websoknad.servlet;

import static java.lang.String.valueOf;

public class ServerUtils {

	public static String getGjenopptaUrl(String requestUrl, String soknadId, String behandlingId) {
	    String fullServerPath =  requestUrl.split("/rest/")[0];
		String gjenopptaUrl = "/soknad/";
		return fullServerPath.concat(gjenopptaUrl).concat(valueOf(soknadId)).concat("#/").concat(behandlingId).concat("/fortsett?utm_source=web&utm_medium=email&utm_campaign=2");
	}
}


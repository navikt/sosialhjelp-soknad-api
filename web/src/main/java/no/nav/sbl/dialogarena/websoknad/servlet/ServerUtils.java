package no.nav.sbl.dialogarena.websoknad.servlet;

import static java.lang.String.valueOf;

public class ServerUtils {

	public static String getGjenopptaUrl(String requestUrl, String behandlingId) {
	    String fullServerPath =  requestUrl.split("/rest/")[0];
		String gjenopptaUrl = "/soknad/";
		return fullServerPath.concat(gjenopptaUrl).concat(valueOf(behandlingId).concat("#/soknad"));
	}
}


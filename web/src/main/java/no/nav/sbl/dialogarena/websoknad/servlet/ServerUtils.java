package no.nav.sbl.dialogarena.websoknad.servlet;

import static java.lang.String.valueOf;

public class ServerUtils {

	public static String getGjenopptaUrl(String requestUrl, long soknadId) {
		String fullServerPath =  requestUrl.split("/rest/")[0];
		String gjenopptaUrl = "/soknad/Dagpenger#/dagpenger/";
		return fullServerPath.concat(gjenopptaUrl).concat(valueOf(soknadId));
	}
}


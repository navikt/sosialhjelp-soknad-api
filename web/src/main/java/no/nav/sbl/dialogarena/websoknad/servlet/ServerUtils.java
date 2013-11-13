package no.nav.sbl.dialogarena.websoknad.servlet;

public class ServerUtils {

	public static String getGjenopptaUrl(String requestUrl, long soknadId) {
		String fullServerPath =  requestUrl.split("/rest/")[0];
		String gjenopptaUrl = "/soknad/Dagpenger#/gjenoppta/";
		return fullServerPath.concat(gjenopptaUrl).concat(String.valueOf(soknadId));
	}
}


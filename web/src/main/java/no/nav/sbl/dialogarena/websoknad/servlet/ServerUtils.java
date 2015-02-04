package no.nav.sbl.dialogarena.websoknad.servlet;

import static java.lang.String.valueOf;

public class ServerUtils {

	public static String getGjenopptaUrl(String requestUrl, String soknadId, String behandlingId) {
        String fullServerPath =  requestUrl.split("/rest/")[0];
        String gjenopptaUrl = String.format("utslagskriterier/%s", behandlingId);
		return fullServerPath.concat(gjenopptaUrl).concat("?utm_source=web&utm_medium=email&utm_campaign=2");
	}

    public static String getEttersendelseUrl(String requestUrl, String behandlingId) {
        String fullServerPath =  requestUrl.split("/rest/")[0];
        String ettersendelse = "/startettersending/";
        return fullServerPath.concat(ettersendelse).concat(valueOf(behandlingId));
    }
}


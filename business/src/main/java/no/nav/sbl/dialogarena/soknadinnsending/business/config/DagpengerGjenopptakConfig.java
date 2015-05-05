package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;

import java.util.Arrays;
import java.util.List;


public class DagpengerGjenopptakConfig implements SoknadConfig {
    public String getSoknadTypePrefix () {
        return "dagpenger.gjenopptak";
    }

    public String getSoknadUrl () {
        return "soknad.dagpenger.gjenopptak.path";
    }

    public String getFortsettSoknadUrl() {
        return "soknad.dagpenger.fortsett.path";
    }

    public String hentStruktur () {
        return "dagpenger_gjenopptak.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 04-16.03", "NAV 04-16.04");
    }

    public List<String> getSoknadBolker() {
        return Arrays.asList(WebSoknadConfig.BOLK_PERSONALIA, WebSoknadConfig.BOLK_BARN);
    }
}

package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;

import java.util.Arrays;
import java.util.List;


public class ForeldrepengerConfig implements SoknadConfig {
    public String getSoknadTypePrefix () {
        return "foreldresoknad";
    }

    public String getSoknadUrl () {
        return "foreldresoknad.path";
    }

    public String getFortsettSoknadUrl() {
        return "foreldresoknad.fortsett.path";
    }

    public String hentStruktur () {
        return "foreldresoknad.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 14-05.06", "NAV 14-05.07", "NAV 14-05.08", "NAV 14-05.09");
    }

    public List<String> getSoknadBolker() {
        return Arrays.asList(WebSoknadConfig.BOLK_PERSONALIA, WebSoknadConfig.BOLK_BARN, WebSoknadConfig.BOLK_ARBEIDSFORHOLD);
    }
}

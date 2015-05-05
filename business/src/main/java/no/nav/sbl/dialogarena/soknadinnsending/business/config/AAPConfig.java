package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;

import java.util.Arrays;
import java.util.List;


public class AAPConfig implements SoknadConfig {
    public String getSoknadTypePrefix () {
        return "aap.ordinaer";
    }

    public String getSoknadUrl () {
        return "soknad.aap.ordinaer.path";
    }

    public String getFortsettSoknadUrl() {
        return "soknad.aap.fortsett.path";
    }

    public String hentStruktur () {
        return "aap_ordinaer.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 11-13.05");
    }

    public List<String> getSoknadBolker() {
        return Arrays.asList(WebSoknadConfig.BOLK_PERSONALIA, WebSoknadConfig.BOLK_BARN, WebSoknadConfig.BOLK_ARBEIDSFORHOLD);
    }
}

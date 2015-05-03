package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;

import java.util.Arrays;
import java.util.List;


public class AAPConfig {
    private static final String SOKNAD_TYPE_PREFIX = "aap.ordinaer";
    private static final String SOKNAD_URL_FASIT_RESSURS = "soknad.aap.ordinaer.path";
    private static final String SOKNAD_FORTSETT_URL_FASIT_RESSURS = "soknad.aap.fortsett.path";
    private static final String STRUKTURDOKUEMENT = "aap_ordinaer.xml";
    private static final List<String> SOKNAD_BOLKER =  Arrays.asList(WebSoknadConfig.BOLK_PERSONALIA, WebSoknadConfig.BOLK_BARN, WebSoknadConfig.BOLK_ARBEIDSFORHOLD);

    public String getSoknadTypePrefix () {
        return SOKNAD_TYPE_PREFIX;
    }

    public String getSoknadUrl () {
        return SOKNAD_URL_FASIT_RESSURS;
    }

    public String getFortsettSoknadUrl() {
        return SOKNAD_FORTSETT_URL_FASIT_RESSURS;
    }

    public String hentStruktur () {
        return STRUKTURDOKUEMENT;
    }

    public List<String> getSoknadBolker() {
        return SOKNAD_BOLKER;
    }
}

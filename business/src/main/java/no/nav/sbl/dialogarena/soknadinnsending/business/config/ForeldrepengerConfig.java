package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.Arrays;
import java.util.List;


public class ForeldrepengerConfig {
    private static final String SOKNAD_TYPE_PREFIX = "foreldresoknad";
    private static final String SOKNAD_URL_FASIT_RESSURS = "foreldresoknad.path";
    private static final String SOKNAD_FORTSETT_URL_FASIT_RESSURS = "foreldresoknad.fortsett.path";
    private static final String STRUKTURDOKUEMENT = "foreldresoknad.xml";
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

package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;

import java.util.Arrays;
import java.util.List;


public class DagpengerGjenopptakConfig {
    private static final String SOKNAD_TYPE_PREFIX = "dagpenger.gjenopptak";
    private static final String SOKNAD_URL_FASIT_RESSURS = "soknad.dagpenger.gjenopptak.path";
    private static final String SOKNAD_FORTSETT_URL_FASIT_RESSURS = "soknad.dagpenger.fortsett.path";
    private static final String STRUKTURDOKUEMENT = "dagpenger_gjenopptak.xml";
    private static final List<String> SOKNAD_BOLKER =  Arrays.asList(WebSoknadConfig.BOLK_PERSONALIA, WebSoknadConfig.BOLK_BARN);

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

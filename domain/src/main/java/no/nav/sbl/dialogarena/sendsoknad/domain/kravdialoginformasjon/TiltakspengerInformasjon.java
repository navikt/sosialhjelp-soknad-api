package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class TiltakspengerInformasjon extends KravdialogInformasjon.DefaultOppsett {
    private static final String FORTSETT_PATH = "tiltakspenger.path";
    private static final String SOKNAD_PATH = "tiltakspenger.path";
    public String getSoknadTypePrefix() {
        return "tiltakspenger";
    }

    public String getSoknadUrlKey() {
        return SOKNAD_PATH;
    }

    public String getFortsettSoknadUrlKey() {
        return FORTSETT_PATH;
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    public String getStrukturFilnavn() {
        return "tiltakspenger.xml";
    }

    public List<String> getSkjemanummer() {
        return Collections.singletonList("NAV 76-13.45");
    }
}
package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.Arrays;
import java.util.List;


public class ForeldrepengerInformasjon implements KravdialogInformasjon {
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
        return Arrays.asList(BOLK_PERSONALIA, BOLK_BARN, BOLK_ARBEIDSFORHOLD);
    }
}

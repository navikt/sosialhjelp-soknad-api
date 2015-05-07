package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.List;

import static java.util.Arrays.asList;


public class AAPInformasjon implements KravdialogInformasjon {
    public String getSoknadTypePrefix () {
        return "aap.ordinaer";
    }

    public String getSoknadUrlKey() {
        return "soknad.aap.ordinaer.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.aap.fortsett.path";
    }

    public String hentStruktur () {
        return "aap_ordinaer.xml";
    }

    public List<String> getSkjemanummer() {
        return asList("NAV 11-13.05");
    }

    public List<String> getSoknadBolker() {
        return asList(BOLK_PERSONALIA, BOLK_BARN, BOLK_ARBEIDSFORHOLD);
    }
}

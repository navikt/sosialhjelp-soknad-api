package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.Arrays;
import java.util.List;


public class DagpengerOrdinaerInformasjon implements KravdialogInformasjon {
    public String getSoknadTypePrefix () {
        return "dagpenger.ordinaer";
    }

    public String getSoknadUrl () {
        return "soknad.dagpenger.ordinaer.path";
    }

    public String getFortsettSoknadUrl() {
        return "soknad.dagpenger.fortsett.path";
    }

    public String hentStruktur () {
        return "dagpenger_ordinaer.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 04-01.03", "NAV 04-01.04");
    }

    public List<String> getSoknadBolker() {
        return Arrays.asList(BOLK_PERSONALIA, BOLK_BARN);
    }
}

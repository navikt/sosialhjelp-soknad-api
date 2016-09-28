package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;

import static java.util.Arrays.asList;


public class AAPOrdinaerInformasjon extends KravdialogInformasjon.DefaultOppsett {

    private static List<String> skjemanummer = asList("NAV 11-13.05");

    public String getSoknadTypePrefix() {
        return "aap.ordinaer";
    }

    public String getSoknadUrlKey() {
        return "soknad.aap.ordinaer.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.aap.fortsett.path";
    }

    public String getStrukturFilnavn() {
        return "aap_ordinaer.xml";
    }

    public List<String> getSkjemanummer() {
        return skjemanummer;
    }

    @Override
    public String getBundleName() {
        return "aap";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

}

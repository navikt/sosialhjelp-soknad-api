package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.List;

import static java.util.Arrays.asList;

public class AAPGjenopptakInformasjon extends KravdialogInformasjon.DefaultOppsett {

    private static List<String> skjemanummer = asList("NAV 11-13.06");

    public String getSoknadTypePrefix() {
        return "aap.gjenopptak";
    }

    public String getSoknadUrlKey() {
        return "soknad.aap.gjenopptak.path";
    }

    public String getFortsettSoknadUrlKey() {
        return  "soknad.aap.fortsett.path";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    public String getStrukturFilnavn() {
        return "aap/aap_gjenopptak.xml";
    }

    @Override
    public boolean brukerNyOppsummering() {
        return true;
    }

    @Override
    public boolean skalSendeMedFullSoknad() {
        return true;
    }

    @Override
    public List<String> getSkjemanummer() {
        return skjemanummer;
    }

    @Override
    public String getBundleName() {
        return "soknadaap";
    }

    @Override
    public boolean brukerEnonicLedetekster() {
        return false;
    }
}

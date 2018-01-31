package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class AAPUtlandetInformasjon extends KravdialogInformasjon.DefaultOppsett {

    private static List<String> skjemanummer = asList("NAV 11-03.07");

    public String getSoknadTypePrefix() {
        return "aap.utland";
    }

    public String getSoknadUrlKey() {
        return "soknad.aap.utland.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.aap.utland.path";
    }

    public String getStrukturFilnavn() {
        return "aap_utland.xml";
    }

    public List<String> getSkjemanummer() {
        return skjemanummer;
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
    public String getBundleName() {
        return "soknad-aap-utland";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.singletonList(BOLK_PERSONALIA);
    }

    @Override
    public boolean brukerEnonicLedetekster() {
        return false;
    }

}

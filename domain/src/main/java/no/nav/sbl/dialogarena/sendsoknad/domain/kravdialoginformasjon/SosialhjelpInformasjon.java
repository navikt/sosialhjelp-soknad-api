package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Arrays;
import java.util.List;

public class SosialhjelpInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public String getSoknadTypePrefix() {
        return "soknadsosialhjelp";
    }

    public String getSoknadUrlKey() {
        return "soknadsosialhjelp.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadsosialhjelp.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList(BOLK_PERSONALIA);
    }

    public String getStrukturFilnavn() {
        return "soknadsosialhjelp.xml";
    }

    //TODO ta i bruk riktig skjemanummer
    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 11-12.10", "NAV 11-12.11");
    }

    @Override
    public String getBundleName() {
        return "soknadsosialhjelp";
    }

    @Override
    public boolean brukerEnonicLedetekster() {
        return false;
    }
}
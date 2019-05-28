package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import java.util.List;

import static java.util.Arrays.asList;

public class SosialhjelpInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public static final String SKJEMANUMMER = "NAV 35-18.01";

    public String getSoknadTypePrefix() {
        return "soknadsosialhjelp";
    }

    public String getSoknadUrlKey() {
        return "soknadsosialhjelp.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadsosialhjelp.fortsett.path";
    }

    public List<String> getSkjemanummer() {
        return asList(SKJEMANUMMER);
    }

    @Override
    public String getBundleName() {
        return "soknadsosialhjelp";
    }
}
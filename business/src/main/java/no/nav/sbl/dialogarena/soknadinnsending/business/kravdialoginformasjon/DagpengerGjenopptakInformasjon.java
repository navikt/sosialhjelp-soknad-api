package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.List;

import static java.util.Arrays.asList;


public class DagpengerGjenopptakInformasjon implements KravdialogInformasjon {
    public String getSoknadTypePrefix () {
        return "dagpenger.gjenopptak";
    }

    public String getSoknadUrlKey() {
        return "soknad.dagpenger.gjenopptak.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknad.dagpenger.fortsett.path";
    }

    public String getStrukturFilnavn() {
        return "dagpenger_gjenopptak.xml";
    }

    public List<String> getSkjemanummer() {
        return asList("NAV 04-16.03", "NAV 04-16.04");
    }

    public List<String> getSoknadBolker() {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }
}

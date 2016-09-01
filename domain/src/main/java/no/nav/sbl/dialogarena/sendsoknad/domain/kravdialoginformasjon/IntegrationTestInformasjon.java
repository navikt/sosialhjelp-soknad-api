package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Arrays;
import java.util.List;


public class IntegrationTestInformasjon extends KravdialogInformasjon.DefaultOppsett {
    @Override
    public String getSoknadTypePrefix() {
        return "integration-1";
    }

    @Override
    public String getSoknadUrlKey() {
        return "foreldresoknad.path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return "foreldresoknad.fortsett.path";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList();
    }

    @Override
    public String getStrukturFilnavn() {
        return "integration-1.xml";
    }

    @Override
    public List<String> getSkjemanummer() {
        return Arrays.asList("INTEGRATION-1");
    }

    @Override
    public String getBundleName() {
        return "integration-1";
    }
}

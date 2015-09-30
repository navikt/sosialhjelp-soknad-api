package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

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
}

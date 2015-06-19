package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoknadTilleggsstonader implements KravdialogInformasjon {

    public String getSoknadTypePrefix() {
        return "soknadtilleggsstonader";
    }

    public String getSoknadUrlKey() {
        return "soknadtilleggsstonader.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadtilleggsstonader.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.singletonList(BOLK_PERSONALIA);
    }

    public String getStrukturFilnavn() {
        return "soknadtilleggsstonader.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 11-22.33");
    }
}

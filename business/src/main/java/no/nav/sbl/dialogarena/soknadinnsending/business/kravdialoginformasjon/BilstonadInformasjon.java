package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BilstonadInformasjon extends KravdialogInformasjon.KravdialogInformasjonUtenAlternativRepresentasjon {

    public String getSoknadTypePrefix() {
        return "bilstonad";
    }

    public String getSoknadUrlKey() {
        return "bilstonad.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "bilstonad.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.singletonList(BOLK_PERSONALIA);
    }

    public String getStrukturFilnavn() {
        return "bilstonad.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 10-07.40", "NAV 10-07.41");
    }
}

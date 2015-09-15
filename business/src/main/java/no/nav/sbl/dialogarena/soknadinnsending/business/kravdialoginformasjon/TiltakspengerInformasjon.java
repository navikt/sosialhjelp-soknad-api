package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class TiltakspengerInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public String getSoknadTypePrefix() {
        return "tiltakspenger";
    }

    public String getSoknadUrlKey() {
        return "tiltakspenger.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "tiltakspenger.path";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        return asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    public String getStrukturFilnavn() {
        return "tiltakspenger.xml";
    }

    public List<String> getSkjemanummer() {
        return Collections.singletonList("NAV 76-13.45");
    }
}
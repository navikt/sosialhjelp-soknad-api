package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Arrays;
import java.util.List;


public class ForeldrepengerInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public static final List<String> STONADSTYPER_PERSONALIA = Arrays.asList("overforing", "engangsstonadMor", "engangsstonadFar");

    public String getSoknadTypePrefix() {
        return "foreldresoknad";
    }

    public String getSoknadUrlKey() {
        return "foreldresoknad.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "foreldresoknad.fortsett.path";
    }

    public String getStrukturFilnavn() {
        return "foreldrepenger/foreldrepenger.xml";
    }

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 14-05.06", "NAV 14-05.07", "NAV 14-05.08", "NAV 14-05.09", "NAV 14-05.10");
    }

    @Override
    public String getBundleName() {
        return "foreldrepenger";
    }

    public List<String> getSoknadBolker(WebSoknad soknad) {
        Faktum stonadstype = soknad.getFaktumMedKey("soknadsvalg.stonadstype");
        if (stonadstype != null && STONADSTYPER_PERSONALIA.contains(stonadstype.getValue())) {
            return Arrays.asList(BOLK_PERSONALIA);
        } else {
            return Arrays.asList(BOLK_PERSONALIA, BOLK_BARN, BOLK_ARBEIDSFORHOLD);
        }
    }

    @Override
    public boolean brukerEnonicLedetekster() {
        return false;
    }
}


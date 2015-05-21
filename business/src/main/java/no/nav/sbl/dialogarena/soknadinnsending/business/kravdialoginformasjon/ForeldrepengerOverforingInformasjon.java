package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.Arrays;
import java.util.List;


public class ForeldrepengerOverforingInformasjon extends ForeldrepengerInformasjon {

    public List<String> getSkjemanummer() {
        return Arrays.asList("NAV 14-05.09");
    }

    public List<String> getSoknadBolker() {
        return Arrays.asList(BOLK_PERSONALIA);
    }
}


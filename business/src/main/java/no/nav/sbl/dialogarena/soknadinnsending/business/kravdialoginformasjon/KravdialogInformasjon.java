package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.List;

public interface KravdialogInformasjon {

    public static final String BOLK_PERSONALIA = "Personalia";
    public static final String BOLK_BARN = "Barn";
    public static final String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";

    String getSoknadTypePrefix();
    String getSoknadUrl();
    String getFortsettSoknadUrl();
    List<String> getSoknadBolker();
    String hentStruktur ();
    List<String> getSkjemanummer();

}

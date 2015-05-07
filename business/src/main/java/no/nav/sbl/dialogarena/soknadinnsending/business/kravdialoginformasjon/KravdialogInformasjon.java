package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import java.util.List;

public interface KravdialogInformasjon {

    String BOLK_PERSONALIA = "Personalia";
    String BOLK_BARN = "Barn";
    String BOLK_ARBEIDSFORHOLD = "Arbeidsforhold";

    String getSoknadTypePrefix();
    String getSoknadUrlKey();
    String getFortsettSoknadUrlKey();
    List<String> getSoknadBolker();
    String hentStruktur ();
    List<String> getSkjemanummer();

}

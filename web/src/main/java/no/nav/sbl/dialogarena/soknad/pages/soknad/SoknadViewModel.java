package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BaseViewModel;

public class SoknadViewModel extends BaseViewModel {

    public SoknadViewModel(String tabTittel, Soknad soknad) {
        super(tabTittel, soknad);
    }

    public String getNavn() {
        return "";
    }

    public String getAdresse() {
        return "";
    }

    public String getEpost() {
        return "";
    }

    public String getInntekt() {
        return "";
    }

    public String getArbeidsgiver() {
        return "";
    }
}
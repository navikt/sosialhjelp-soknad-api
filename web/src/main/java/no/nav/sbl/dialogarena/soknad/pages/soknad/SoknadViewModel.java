package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.convert.xml.XmlSoknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BaseViewModel;

public class SoknadViewModel extends BaseViewModel {

    public SoknadViewModel(String tabTittel, XmlSoknad soknad) {
        super(tabTittel, soknad);
    }

//    public List<InputElement> getInputList() {
//        return soknad.getInputNodes();
//    }

    public String getNavn() {
        return soknad.getValue("navn");
    }

    public String getAdresse() {
        return soknad.getValue("adresse");
    }

    public String getEpost() {
        return soknad.getValue("epost");
    }

    public String getInntekt() {
        return soknad.getValue("inntekt");
    }

    public String getArbeidsgiver() {
        return soknad.getValue("arbeidsgiver");
    }
}
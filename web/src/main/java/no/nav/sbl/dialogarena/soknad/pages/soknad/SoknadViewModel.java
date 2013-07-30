package no.nav.sbl.dialogarena.soknad.pages.soknad;

import no.nav.sbl.dialogarena.soknad.convert.InputElement;
import no.nav.sbl.dialogarena.soknad.convert.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.felles.BaseViewModel;

import java.util.List;

public class SoknadViewModel extends BaseViewModel {

    private Soknad soknad;

    public SoknadViewModel(String tabTittel, Soknad soknad) {
        super(tabTittel);
        this.soknad = soknad;
    }

    public List<InputElement> getInputList() {
        return soknad.getInputNodes();
    }
}

package no.nav.sbl.dialogarena.soknad.pages.felles.input.radiogruppe;

import no.nav.sbl.dialogarena.soknad.domain.Faktum;

import java.io.Serializable;
import java.util.List;

public class RadiogruppeViewModel implements Serializable {

    private Faktum faktum;
    private List<String> valgliste;

    public RadiogruppeViewModel(Faktum faktum, List<String> valgliste) {
        this.faktum = faktum;
        this.valgliste = valgliste;
    }

    public String getRadiogruppe() {
        return faktum.getValue();
    }

    public List<String> getValgliste() {
        return valgliste;
    }
}

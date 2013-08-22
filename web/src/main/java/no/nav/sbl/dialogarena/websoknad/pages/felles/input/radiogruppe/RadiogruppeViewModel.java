package no.nav.sbl.dialogarena.websoknad.pages.felles.input.radiogruppe;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;

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
        String value = faktum.getValue();
        if (value == null) {
            return "";
        }
        return value;
    }

    public List<String> getValgliste() {
        return valgliste;
    }
}

package no.nav.sbl.dialogarena.soknad.pages.felles.input;

import no.nav.sbl.dialogarena.soknad.domain.Faktum;

import java.io.Serializable;

public class FaktumViewModel implements Serializable {
    private Faktum faktum;
    private String label;

    public FaktumViewModel(Faktum faktum, String label) {
        this.faktum = faktum;
        this.label = label;
    }

    public String getValue() {
        return faktum.value;
    }

    public String getLabel() {
        return label;
    }

    public Faktum getFaktum() {
        return faktum;
    }
}

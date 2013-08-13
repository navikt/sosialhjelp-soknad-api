package no.nav.sbl.dialogarena.soknad.pages.felles.input;

import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class FaktumViewModel implements Serializable {
    private Faktum faktum;
    private String label;

    public FaktumViewModel(Faktum faktum, String label) {
        this.faktum = faktum;
        this.label = label;
    }

    public void setValue(String value) {
        faktum.setValue(value);
    }

    public Boolean getBooleanValue() {
        String value = getValue();
        if (StringUtils.isBlank(value) || value.equalsIgnoreCase("false")) {
            return false;
        }

        return true;
    }

    public String getValue() {
        String value = faktum.getValue();
        if (value == null) {
            return "";
        }
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelLowerCase() {
        return label.toLowerCase();
    }

    public Faktum getFaktum() {
        return faktum;
    }
}

package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import javax.xml.bind.annotation.XmlIDREF;
import java.io.Serializable;

public class SoknadVedlegg implements Serializable {

    private SoknadFaktum faktum;
    private String onValue;
    private String gosysId;
    private String property;

    @XmlIDREF
    public SoknadFaktum getFaktum() {
        return faktum;
    }

    public void setFaktum(SoknadFaktum faktum) {
        this.faktum = faktum;
    }

    public String getOnValue() {
        return onValue;
    }

    public void setOnValue(String onValue) {
        this.onValue = onValue;
    }

    public String getGosysId() {
        return gosysId;
    }

    public void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return new StringBuilder("SoknadVedlegg{")
                .append("faktum=").append(faktum)
                .append(", onValue='").append(onValue).append('\'')
                .append(", gosysId='").append(gosysId).append('\'')
                .append('}')
                .toString();
    }

    public boolean trengerVedlegg(String value) {
        return onValue == null || onValue.equalsIgnoreCase(value);
    }
}

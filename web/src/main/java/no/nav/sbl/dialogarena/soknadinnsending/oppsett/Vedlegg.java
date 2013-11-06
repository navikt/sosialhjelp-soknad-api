package no.nav.sbl.dialogarena.soknadinnsending.oppsett;


import javax.xml.bind.annotation.XmlIDREF;
import java.io.Serializable;

public class Vedlegg implements Serializable {
    private Faktum faktum;
    private String onValue;
    private String gosysId;

    @XmlIDREF
    public Faktum getFaktum() {
        return faktum;
    }

    public void setFaktum(Faktum faktum) {
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vedlegg{");
        sb.append("faktum=").append(faktum);
        sb.append(", onValue='").append(onValue).append('\'');
        sb.append(", gosysId='").append(gosysId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

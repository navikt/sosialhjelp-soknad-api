package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import org.apache.commons.lang3.builder.ToStringBuilder;

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


    public boolean trengerVedlegg(String value) {
        return onValue == null || onValue.equalsIgnoreCase(value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("faktum", faktum)
                .append("onValue", onValue)
                .append("gosysId", gosysId)
                .append("property", property)
                .toString();
    }
}

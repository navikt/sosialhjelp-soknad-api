package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import java.io.Serializable;

public class SoknadFaktum implements Serializable {

    private String id;
    private String type;
    private SoknadFaktum dependOn;
    private String dependOnValue;

    @XmlID()
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlIDREF
    public SoknadFaktum getDependOn() {
        return dependOn;
    }

    public void setDependOn(SoknadFaktum dependOn) {
        this.dependOn = dependOn;
    }

    public String getDependOnValue() {
        return dependOnValue;
    }

    public void setDependOnValue(String dependOnValue) {
        this.dependOnValue = dependOnValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("dependOn", dependOn)
                .append("dependOnValue", dependOnValue)
                .toString();
    }
}

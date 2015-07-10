package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class SoknadFaktum implements Serializable {

    private String id;
    private String type;
    private SoknadFaktum dependOn;

    private String dependOnProperty;
    private List<String> dependOnValues;

    private String flereTillatt;
    private String erSystemFaktum;

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

    @XmlElementWrapper(name = "dependOnValues")
    @XmlElement(name = "value")
    public List<String> getDependOnValues() {
        return dependOnValues;
    }

    public void setDependOnValues(List<String> values) {
        dependOnValues = values;
    }

    public SoknadFaktum medDependOnValues(List<String> values) {
        setDependOnValues(values);
        return this;
    }

    public String getDependOnProperty() {
        return dependOnProperty;
    }

    public void setDependOnProperty(String dependOnProperty) {
        this.dependOnProperty = dependOnProperty;
    }

    public String getFlereTillatt() { return flereTillatt; }

    public void setFlereTillatt(String flereTillatt) { this.flereTillatt = flereTillatt; }

    public String getErSystemFaktum() { return erSystemFaktum; }

    public void setErSystemFaktum(String erSystemFaktum) { this.erSystemFaktum = erSystemFaktum; }

    public SoknadFaktum medDependOn(SoknadFaktum parent) {
        this.dependOn = parent;
        return this;
    }

    public SoknadFaktum medId(String id) {
        this.setId(id);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("dependOn", dependOn)
                .append("dependOnProperty", dependOnProperty)
                .append("dependOnValues", dependOnValues.toString())
                .append("flereTillatt", flereTillatt)
                .append("erSystemFaktum", erSystemFaktum)
                .toString();
    }

    /*
    For å sortere etter dependency til andre faktum. Faktum uten depdenceny kommer
    før faktum med dependency.
     */
    public static Comparator<SoknadFaktum> sammenlignEtterDependOn() {
        return new Comparator<SoknadFaktum>() {
            @Override
            public int compare(SoknadFaktum sf1, SoknadFaktum sf2) {
                if (sf1.getDependOn() == null && sf2.getDependOn() == null) {
                    return 0;
                } else if (sf1.getDependOn() == null) {
                    return -1;
                } else if (sf2.getDependOn() == null) {
                    return 1;
                } else if (sf1.getId().equals(sf2.getDependOn().getId())) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }
}

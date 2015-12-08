package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.ForventningsSjekker.sjekkForventning;

public class FaktumStruktur implements Serializable, StrukturConfigurable {

    private String id;
    private String type;
    private String panel;
    private FaktumStruktur dependOn;

    private String dependOnProperty;
    private List<String> dependOnValues;
    private boolean useExpression = false;

    private String flereTillatt;
    private String erSystemFaktum;
    private List<PropertyStruktur> properties;
    private List<String> constraints;

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

    public String getPanel() {
        return panel;
    }

    public void setPanel(String bolk) {
        this.panel = bolk;
    }

    @XmlIDREF
    public FaktumStruktur getDependOn() {
        return dependOn;
    }

    public void setDependOn(FaktumStruktur dependOn) {
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

    public FaktumStruktur medDependOnValues(List<String> values) {
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

    public boolean isUseExpression() {
        return useExpression;
    }

    public void setUseExpression(boolean useExpression) {
        this.useExpression = useExpression;
    }

    public FaktumStruktur medDependOn(FaktumStruktur parent) {
        this.dependOn = parent;
        return this;
    }
    public FaktumStruktur medDependOnProperty(String dependOnProperty) {
        this.dependOnProperty = dependOnProperty;
        return this;
    }

    public FaktumStruktur medId(String id) {
        this.setId(id);
        return this;
    }

    @XmlElement(name = "property")
    @XmlElementWrapper(name="properties")
    public List<PropertyStruktur> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyStruktur> properties) {
        this.properties = properties;
    }

    @XmlElementWrapper(name="constraints")
    @XmlElement(name="constraint")
    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("panel", panel)
                .append("dependOn", dependOn)
                .append("dependOnProperty", dependOnProperty)
                .append("dependOnValues", dependOnValues)
                .append("useExpression", useExpression)
                .append("flereTillatt", flereTillatt)
                .append("erSystemFaktum", erSystemFaktum)
                .append("properties", properties)
                .append("constraints", constraints)
                .toString();
    }

    /*
    For å sortere etter dependency til andre faktum. Faktum uten depdenceny kommer
    før faktum med dependency.
     */
    public static Comparator<FaktumStruktur> sammenlignEtterDependOn() {
        return new Comparator<FaktumStruktur>() {
            @Override
            public int compare(FaktumStruktur sf1, FaktumStruktur sf2) {
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

    public boolean erSynlig(WebSoknad soknad, Faktum faktum) {
        FaktumStruktur parent = getDependOn();
        Faktum parentFaktum = soknad.finnFaktum(faktum.getParrentFaktum());
        return (parent == null || (parentFaktum != null && parent.erSynlig(soknad, parentFaktum) && this.oppfyllerParentKriterier(soknad, faktum))) && oppfyllerConstraints(faktum);
    }

    private boolean oppfyllerParentKriterier(WebSoknad soknad, Faktum faktum) {
        Faktum parent = faktum.getParrentFaktum() != null ? soknad.finnFaktum(faktum.getParrentFaktum()): soknad.getFaktumMedKey(getDependOn().getId());
        if(parent != null && getDependOnValues() != null && getDependOnValues().size() > 0){
            if(!useExpression){
                return (harDependOnProperty(parent) || harDependOnValue(parent));
            } else {
                boolean result = false;
                for (String dependOnValue : dependOnValues) {
                    result = result || sjekkForventning(dependOnValue, parent);
                }
                return result;
            }
        }
        return true;
    }

    private boolean oppfyllerConstraints(Faktum faktum) {
        if (constraints == null) {
            return true;
        }

        boolean result = false;
        for (String constraint : constraints) {
            result = result || sjekkForventning(constraint, faktum);
        }
        return result;
    }

    private boolean harDependOnValue(Faktum parent) {
        return dependOnProperty == null && parent.harValueSomMatcher(dependOnValues);
    }

    private boolean harDependOnProperty(Faktum parent) {
        return dependOnProperty != null && parent.harPropertySomMatcher(dependOnProperty, dependOnValues);
    }

    public boolean ikkeFlereTillatt() {
        return !"true".equals(this.getFlereTillatt());
    }

    public boolean ikkeSystemFaktum() {
        return !"true".equals(this.getErSystemFaktum());
    }

    @Override
    public Map<String, String> getConfiguration() {
        return new HashMap<>();
    }

    public boolean hasConfig(String configKey) {
        return getConfiguration() != null && getConfiguration().containsKey(configKey);
    }

}

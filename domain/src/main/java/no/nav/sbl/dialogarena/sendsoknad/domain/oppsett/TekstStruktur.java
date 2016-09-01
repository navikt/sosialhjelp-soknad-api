package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlType(propOrder = {})
public class TekstStruktur implements Serializable, StrukturConfigurable {

    public static String INFOTEKST = "infotekst";
    public static String HJELPETEKST = "hjelpetekst";

    private String type;
    private List<String> dependOnValues;
    private String key;
    private List<Constraint> constraints;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElementWrapper(name = "dependOnValues")
    @XmlElement(name = "value")
    public List<String> getDependOnValues() {
        return dependOnValues;
    }

    public void setDependOnValues(List<String> dependOnValues) {
        this.dependOnValues = dependOnValues;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @XmlElementWrapper(name="constraints")
    @XmlElement(name="constraint")
    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration();
    }
}

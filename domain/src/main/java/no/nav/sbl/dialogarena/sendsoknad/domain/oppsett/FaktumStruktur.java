package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;


import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.TekstStruktur.HJELPETEKST;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.TekstStruktur.INFOTEKST;

@XmlType(propOrder = {})
public class FaktumStruktur implements Serializable, StrukturConfigurable {

    private String id;
    private String type;
    private String panel;
    private FaktumStruktur dependOn;

    private String dependOnProperty;
    private List<String> dependOnValues;
    private Boolean useExpression = false;
    private Boolean kunUtvidet = false;
    private Boolean optional = false;

    private String flereTillatt;
    private String erSystemFaktum;
    private List<PropertyStruktur> properties;
    private List<Constraint> constraints;
    private List<TekstStruktur> tekster;

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

    public Boolean isUseExpression() {
        return useExpression;
    }

    public void setUseExpression(Boolean useExpression) {
        this.useExpression = useExpression;
    }

    public Boolean isOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
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
    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    @XmlElementWrapper(name="tekster")
    @XmlElement(name="tekst")
    public List<TekstStruktur> getTekster() {
        return tekster;
    }

    public void setTekster(List<TekstStruktur> tekster) {
        this.tekster = tekster;
    }

    @XmlAttribute(name="kunUtvidet")
    public Boolean getKunUtvidet() {
        return kunUtvidet;
    }

    public void setKunUtvidet(Boolean kunUtvidet) {
        this.kunUtvidet = kunUtvidet;
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
                .append("optional", optional)
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
        return parentErAktiv(soknad, faktum)
                && oppfyllerConstraints(soknad, faktum);
    }

    private boolean parentErAktiv(WebSoknad soknad, Faktum faktum) {
        FaktumStruktur parent = getDependOn();
        Faktum parentFaktum = soknad.finnFaktum(faktum.getParrentFaktum());
        return parent == null || (parentFaktum != null &&
                parent.erSynlig(soknad, parentFaktum)
                && this.oppfyllerParentKriterier(soknad, faktum));
    }

    private boolean oppfyllerParentKriterier(WebSoknad soknad, Faktum faktum) {
        Faktum parent = faktum.getParrentFaktum() != null ? soknad.finnFaktum(faktum.getParrentFaktum()): soknad.getFaktumMedKey(getDependOn().getId());
        if(parent != null && getDependOnValues() != null && !getDependOnValues().isEmpty()){
            if(!useExpression){
                return harDependOnProperty(parent) || harDependOnValue(parent);
            } else {
                boolean result = false;
                for (String dependOnValue : dependOnValues) {
                    result = result || ForventningsSjekker.sjekkForventning(dependOnValue, parent);
                }
                return result;
            }
        }
        return true;
    }

    protected boolean oppfyllerConstraints(WebSoknad soknad, Faktum faktum) {
        if (constraints == null) {
            return true;
        }

        boolean result = false;
        for (Constraint constraint : constraints) {
            Faktum constraintFaktum = getConstraintFaktum(constraint, soknad, faktum);
            result = result || ForventningsSjekker.sjekkForventning(constraint.getExpression(), constraintFaktum);
        }
        return result;
    }

    public List<TekstStruktur> getInfotekster(WebSoknad soknad, Faktum faktum) {
        if (tekster == null) {
            return new ArrayList<>();
        }

        return on(tekster)
                .filter(tekstOppfyllerDependOn(faktum))
                .filter(tekstStrukturOppfyllerConstraints(soknad, faktum))
                .filter(tekstErType(INFOTEKST))
                .collect();
    }

    public List<TekstStruktur> getHjelpetekster(WebSoknad soknad, Faktum faktum) {
        if (tekster == null) {
            return new ArrayList<>();
        }

        return on(tekster)
                .filter(tekstOppfyllerDependOn(faktum))
                .filter(tekstStrukturOppfyllerConstraints(soknad, faktum))
                .filter(tekstErType(HJELPETEKST))
                .collect();
    }

    private Predicate<TekstStruktur> tekstStrukturOppfyllerConstraints(final WebSoknad soknad, final Faktum faktum) {
        return new Predicate<TekstStruktur>() {
            @Override
            public boolean evaluate(TekstStruktur tekst) {
                for(Constraint constraint: tekst.getConstraints()) {
                    Faktum constraintFaktum = getConstraintFaktum(constraint, soknad, faktum);
                    if(!ForventningsSjekker.sjekkForventning(constraint.getExpression(), constraintFaktum)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private Predicate<TekstStruktur> tekstOppfyllerDependOn(final Faktum faktum) {
        return new Predicate<TekstStruktur>() {
            @Override
            public boolean evaluate(TekstStruktur tekst) {
                List<String> tekstDependOnValues = tekst.getDependOnValues();
                if (tekstDependOnValues == null || tekstDependOnValues.isEmpty()) {
                    return true;
                }
                return tekstDependOnValues.contains(faktum.getValue());
            }
        };
    }

    private Predicate<TekstStruktur> tekstErType(final String type) {
        return new Predicate<TekstStruktur>() {
            @Override
            public boolean evaluate(TekstStruktur tekst) {
                return tekst.getType().equals(type);
            }
        };
    }

    private Faktum getConstraintFaktum(Constraint constraint, WebSoknad soknad, Faktum faktum) {
        Faktum constraintFaktum = faktum;
        if (constraint.getFaktum() != null && !constraint.getFaktum().isEmpty()) {
            constraintFaktum = soknad.getFaktumMedKey(constraint.getFaktum());
        }
        return constraintFaktum;
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
    public Configuration getConfiguration() {
        return new Configuration();
    }

    public boolean hasConfig(String configKey) {
        return getConfiguration() != null && getConfiguration().containsKey(configKey);
    }

}

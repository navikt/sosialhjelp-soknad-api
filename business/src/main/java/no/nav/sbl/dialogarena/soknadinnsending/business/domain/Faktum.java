package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Faktum implements Serializable {
    private Long faktumId;
    private Long soknadId;
    private Long parrentFaktum;
    private String key;
    private String value;
    private Set<FaktumEgenskap> faktumEgenskaper;
    private Map<String, String> properties = new HashMap<>();
    private FaktumType type;

    public Long getFaktumId() {
        return faktumId;
    }

    public void setFaktumId(Long faktumId) {
        this.faktumId = faktumId;
        for (FaktumEgenskap egenskap : getFaktumEgenskaper()) {
            egenskap.setFaktumId(faktumId);
        }
    }

    public String getValue() {
        return value;
    }

    public final void setValue(String value) {
        this.value = value;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public FaktumType getType() {
        return type;
    }

    public void setType(FaktumType type) {
        this.type = type;
    }

    @JsonIgnore
    public String getTypeString() {
        return getType().toString();
    }

    public Long getParrentFaktum() {
        return parrentFaktum;
    }

    public void setParrentFaktum(Long parrentFaktum) {
        this.parrentFaktum = parrentFaktum;
    }

    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Set<FaktumEgenskap> getFaktumEgenskaper() {
        if (faktumEgenskaper == null) {
            setFaktumEgenskaper(new HashSet<FaktumEgenskap>());
        }
        return faktumEgenskaper;
    }

    public void setFaktumEgenskaper(Set<FaktumEgenskap> faktumEgenskaper) {
        this.faktumEgenskaper = faktumEgenskaper;
    }

    public Faktum medKey(String key) {
        setKey(key);
        return this;
    }

    public Faktum medValue(String value) {
        setValue(value);
        return this;
    }

    public Faktum medSoknadId(Long soknadId) {
        setSoknadId(soknadId);
        return this;
    }

    public Faktum medType(FaktumType type) {
        setType(type);
        return this;
    }

    public Faktum medFaktumId(Long faktumId) {
        setFaktumId(faktumId);
        return this;
    }

    public Faktum medParrentFaktumId(Long faktumId) {
        setParrentFaktum(faktumId);
        return this;
    }

    public Faktum medProperty(String key, String value) {
        medEgenskap(new FaktumEgenskap(soknadId, faktumId, key, value, false));
        return this;
    }

    public void medEgenskap(FaktumEgenskap faktumEgenskap) {
        getFaktumEgenskaper().add(faktumEgenskap);
        getProperties().put(faktumEgenskap.getKey(), faktumEgenskap.getValue());
    }

    public Faktum medSystemProperty(String key, String value) {
        medEgenskap(new FaktumEgenskap(soknadId, faktumId, key, value, true));
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("faktumId", faktumId)
                .append("soknadId", soknadId)
                .append("parrentFaktum", parrentFaktum)
                .append("key", key)
                .append("value", value)
                .append("properties", properties)
                .append("type", type)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Faktum rhs = (Faktum) obj;
        return new EqualsBuilder()
                .append(this.faktumId, rhs.faktumId)
                .append(this.soknadId, rhs.soknadId)
                .append(this.parrentFaktum, rhs.parrentFaktum)
                .append(this.key, rhs.key)
                .append(this.value, rhs.value)
                .append(this.properties, rhs.properties)
                .append(this.type, rhs.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(faktumId)
                .append(soknadId)
                .append(parrentFaktum)
                .append(key)
                .append(value)
                .append(properties)
                .append(type)
                .toHashCode();
    }

    public boolean hasEgenskap(String key) {
        boolean res = false;
        for (FaktumEgenskap egenskap : faktumEgenskaper) {
            res = res || egenskap.getKey().equals(key);
        }
        return res;
    }

    public void kopierBrukerlagrede(Faktum lagretFaktum) {
        for (FaktumEgenskap egenskap : lagretFaktum.getFaktumEgenskaper()) {
            if (egenskap.getSystemEgenskap().equals(0) && !this.hasEgenskap(egenskap.getKey())) {
                medEgenskap(egenskap);
            }
        }
    }

    public void kopierSystemlagrede(Faktum lagretFaktum) {
        for (FaktumEgenskap egenskap : lagretFaktum.getFaktumEgenskaper()) {
            if (egenskap.getSystemEgenskap().equals(1)) {
                fjernEgenskapMedNokkel(egenskap.getKey());
                medEgenskap(egenskap);
            }
        }
    }

    private void fjernEgenskapMedNokkel(String key) {
        Iterator<FaktumEgenskap> iterator = faktumEgenskaper.iterator();
        while (iterator.hasNext()) {
            FaktumEgenskap next = iterator.next();
            if (next.getKey().equals(key)) {
                iterator.remove();
            }
        }
    }

    public boolean er(FaktumType systemregistrert) {
        return getType().equals(systemregistrert);
    }

    public void kopierFraProperies() {
        getFaktumEgenskaper().clear();
        for (String k : getProperties().keySet()) {
            faktumEgenskaper.add(new FaktumEgenskap(soknadId, faktumId, k, getProperties().get(k), false));
        }
    }

    public boolean matcherUnikProperty(String uniqueProperty, Faktum faktum) {
        return harEgenskap(uniqueProperty)
                && getProperties().get(uniqueProperty).equals(faktum.getProperties().get(uniqueProperty));
    }

    private boolean harEgenskap(String uniqueProperty) {
        return getProperties().get(uniqueProperty) != null;
    }

    public enum FaktumType {SYSTEMREGISTRERT, BRUKERREGISTRERT}

    public enum Status {
        IkkeVedlegg,
        VedleggKreves,
        LastetOpp,
        SendesSenere,
        SendesIkke;

        public boolean er(Status status) {
            return this.equals(status);
        }
    }
}
package no.nav.sbl.dialogarena.sendsoknad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.toList;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Faktum implements Serializable {
    public static final String UNIQUE_KEY = "uniqueKey";
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

    public void setValue(String value) {
        this.value = value;
    }


    public Long getSoknadId() {
        return soknadId;
    }

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        for (FaktumEgenskap egenskap : getFaktumEgenskaper()) {
            egenskap.setSoknadId(soknadId);
        }
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

    public Map<String,String> getDatoProperties(){
        Map<String,String> datoProperties = new HashMap<>();
         getProperties().entrySet().stream()
                 .filter(property -> datoKeys().contains(property.getKey()))
                 .forEach(property -> datoProperties.put(property.getKey(),property.getValue()));

         return datoProperties;
    }

    public boolean hasDatoProperty(){
       return getProperties().keySet()
               .stream()
               .anyMatch(key -> datoKeys().contains(key));
    }

    private static Set<String> datoKeys(){
        HashSet<String> datoKeys = new HashSet();
        datoKeys.add("fom");
        datoKeys.add("tom");
        datoKeys.add("fradato");
        datoKeys.add("tildato");
        return datoKeys;
    }

    @JsonIgnore
    public String getUnikProperty(){
        return getProperties().get(UNIQUE_KEY);
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
        if (finnEgenskap(key) != null) {
            finnEgenskap(key).setValue(value);
            getProperties().put(key, value);
        } else {
            medEgenskap(new FaktumEgenskap(soknadId, faktumId, key, value, false));
        }
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

    public FaktumEgenskap finnEgenskap(String key) {
        for (FaktumEgenskap egenskap : getFaktumEgenskaper()) {
            if (egenskap.getKey().equals(key)) {
                return egenskap;
            }
        }
        return null;
    }

    /**
     * kopierer over alle brukerlagrede faktum fra det gitte faktumet til dette faktumet.
     * Ment brukt i tilfeller der en lagrer systemfaktum.
     *
     * @param lagretFaktum det afktumet properties skal hentes fra
     */
    public void kopierFaktumegenskaper(Faktum lagretFaktum) {
        for (FaktumEgenskap egenskap : lagretFaktum.getFaktumEgenskaper()) {
            if (egenskap.getSystemEgenskap().equals(0) && !this.hasEgenskap(egenskap.getKey())) {
                medEgenskap(egenskap);
            }
        }
    }

    public void kopierSystemlagrede(Faktum lagretFaktum) {
        fjernSystemegenskaper();
        for (FaktumEgenskap egenskap : lagretFaktum.getFaktumEgenskaper()) {
            if (egenskap.getSystemEgenskap().equals(1)) {
                fjernEgenskapMedNokkel(egenskap.getKey());
                medEgenskap(egenskap);
            }
        }
    }

    private void fjernSystemegenskaper() {
        Iterator<FaktumEgenskap> iterator = faktumEgenskaper.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getSystemEgenskap() == 1) {
                iterator.remove();
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
        return matcherUnikProperty(uniqueProperty, faktum.getProperties().get(uniqueProperty));
    }
    public boolean matcherUnikProperty(String uniqueProperty, String value) {
        return harEgenskap(uniqueProperty)
                && getProperties().get(uniqueProperty).equals(value);
    }

    private boolean harEgenskap(String uniqueProperty) {
        return getProperties().get(uniqueProperty) != null;
    }

    public Faktum medUnikProperty(String unikRef) {
        return medSystemProperty(UNIQUE_KEY, unikRef);
    }

    public boolean harPropertySomMatcher(String dependOnProperty, String... dependOnValues) {
        return harPropertySomMatcher(dependOnProperty, Arrays.asList(dependOnValues));
    }
    public boolean harPropertySomMatcher(String dependOnProperty, List<String> dependOnValues) {
        return harEgenskap(dependOnProperty) && dependOnValues.contains(properties.get(dependOnProperty));
    }

    public boolean harValueSomMatcher(List<String> dependOnValues) {
        return getValue() != null && dependOnValues.contains(getValue());
    }

    public boolean hasValue() {
        return getValue() != null;
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
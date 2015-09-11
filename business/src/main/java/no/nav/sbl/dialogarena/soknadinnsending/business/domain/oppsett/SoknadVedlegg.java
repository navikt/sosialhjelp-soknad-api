package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SoknadVedlegg implements Serializable {

    private SoknadFaktum faktum;
    private String onProperty;
    private Boolean forSystemfaktum;
    private Boolean flereTillatt = false;
    private String skjemaNummer;
    private String skjemanummerTillegg;
    private String property;
    private Boolean inverted = false;
    private String oversetting;
    private List<String> ekstraValg = new ArrayList<>();
    private List<String> values = new ArrayList<>();
    private String filterKey;
    private List<String> filterValues = new ArrayList<>();

    @XmlIDREF
    public SoknadFaktum getFaktum() {
        return faktum;
    }

    public void setFaktum(SoknadFaktum faktum) {
        this.faktum = faktum;
    }

    @XmlElementWrapper(name = "onValues")
    @XmlElement(name = "value")
    public List<String> getOnValues() {
        return values;
    }

    public void setOnValues(List<String> values) {
        this.values = values;
    }

    public String getOnProperty() {
        return onProperty;
    }

    public void setOnProperty(String onProperty) {
        this.onProperty = onProperty;
    }

    public String getSkjemaNummer() {
        return skjemaNummer;
    }

    public void setSkjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
    }

    public String getSkjemanummerTillegg() {
        return skjemanummerTillegg;
    }

    public void setSkjemanummerTillegg(String tillegg) {
        this.skjemanummerTillegg = tillegg;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    //Sett denne om en skal sjekke om verdien ikke er lik det som står i onValues
    public Boolean getInverted() {
        return inverted;
    }

    public void setInverted(Boolean inverted) {
        this.inverted = inverted;
    }

    public String getOversetting() {
        return oversetting;
    }

    public void setOversetting(String oversetting) {
        this.oversetting = oversetting;
    }

    @XmlElementWrapper(name = "ekstraValg")
    @XmlElement(name = "valg")
    public List<String> getEkstraValg() {
        return ekstraValg;
    }

    public void setEkstraValg(List<String> valg) {
        this.ekstraValg = valg;
    }

    public boolean harOversetting() {
        return StringUtils.isNotEmpty(this.oversetting);
    }

    public String getFilterKey() {
        return filterKey;
    }

    public void setFilterKey(String filterKey) {
        this.filterKey = filterKey;
    }

    @XmlElementWrapper(name = "filterValues")
    @XmlElement(name = "value")
    public List<String> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(List<String> filterValues) {
        this.filterValues = filterValues;
    }

    public boolean trengerVedlegg(Faktum value) {
        String valToCheck;
        if (forSystemfaktum == null || forSystemfaktum || value.er(FaktumType.BRUKERREGISTRERT)) {
            if (onProperty != null) {
                valToCheck = value.getProperties().get(onProperty);
            } else {
                valToCheck = value.getValue();
            }
            if (inverted == null || !inverted) {
                return doesValueMatch(valToCheck);
            } else {
                return !doesValueMatch(valToCheck);
            }
        }
        return false;
    }

    private boolean doesValueMatch(String valToCheck) {
        if (values == null || values.isEmpty()) {
            return true;
        }
        for (String value : values) {
            if (value.equalsIgnoreCase(valToCheck)) {
                return true;
            }

        }
        return false;
    }

    public Boolean getForSystemfaktum() {
        return forSystemfaktum;
    }

    public void setForSystemfaktum(Boolean forSystemfaktum) {
        this.forSystemfaktum = forSystemfaktum;
    }

    public Boolean getFlereTillatt() {
        return flereTillatt;
    }

    public void setFlereTillatt(Boolean flereTillatt) {
        this.flereTillatt = flereTillatt;
    }

    public SoknadFaktum getParentFaktum() {
        return getFaktum().getDependOn();
    }

    public boolean harParent() {
        return getFaktum().getDependOn() != null;
    }

    public SoknadVedlegg medFaktum(SoknadFaktum faktum) {
        this.setFaktum(faktum);
        return this;
    }

    public SoknadVedlegg medOnValues(List<String> dependOnValues) {
        this.setOnValues(dependOnValues);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("faktum", faktum)
                .append("onProperty", onProperty)
                .append("forSystemfaktum", forSystemfaktum)
                .append("flereTillatt", flereTillatt)
                .append("skjemaNummer", skjemaNummer)
                .append("property", property)
                .append("inverted", inverted)
                .append("oversetting", oversetting)
                .append("values", values)
                .toString();
    }

    public boolean harFilterProperty(Faktum faktum) {
        return filterKey == null ||
                filterValues.contains(faktum.getProperties().get(filterKey));
    }
}

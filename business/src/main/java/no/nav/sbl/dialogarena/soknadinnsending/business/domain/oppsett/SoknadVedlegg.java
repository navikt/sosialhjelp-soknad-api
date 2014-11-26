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
import java.util.Arrays;
import java.util.List;

public class SoknadVedlegg implements Serializable {

    private SoknadFaktum faktum;
    private String onProperty;
    private Boolean forSystemfaktum;
    private Boolean flereTillatt = false;
    private String skjemaNummer;
    private String property;
    private Boolean inverted = false;
    private String oversetting;
    private List<String> ekstraValg = new ArrayList<>();
    private List<String> values = new ArrayList<>();

    @XmlIDREF
    public SoknadFaktum getFaktum() {
        return faktum;
    }

    public void setFaktum(SoknadFaktum faktum) {
        this.faktum = faktum;
    }

    public String getOnValue() {
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public void setOnValue(String onValue) {
        this.values = Arrays.asList(onValue);
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

    public String getSkjemaNummerFiltrert() {
        if (getSkjemaNummer() != null && getSkjemaNummer().contains("|")) {
            return getSkjemaNummer().substring(0, getSkjemaNummer().indexOf("|"));
        }
        return getSkjemaNummer();
    }

    public void setSkjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
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
}

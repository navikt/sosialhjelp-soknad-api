package no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;

import javax.xml.bind.annotation.XmlIDREF;
import java.io.Serializable;

import static org.slf4j.LoggerFactory.getLogger;

public class SoknadVedlegg implements Serializable {

    private static final Logger logger = getLogger(SoknadVedlegg.class);
    private SoknadFaktum faktum;
    private String onValue;
    private String onProperty;
    private Boolean forSystemfaktum;
    private Boolean flereTillatt = false;
    private String skjemaNummer;
    private String property;
    private Boolean inverted = false;

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

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Boolean getInverted() {
        return inverted;
    }

    public void setInverted(Boolean inverted) {
        this.inverted = inverted;
    }

    public boolean trengerVedlegg(Faktum value) {
        String valToCheck;
        if (forSystemfaktum == null || forSystemfaktum || value.getType().equals(FaktumType.BRUKERREGISTRERT.name())) {
            if (onProperty != null) {
                valToCheck = value.getProperties().get(onProperty);
            } else {
                valToCheck = value.getValue();
            }
            if (inverted == null || !inverted) {
                return onValue == null || onValue.equalsIgnoreCase(valToCheck);
            } else {
                return !onValue.equalsIgnoreCase(valToCheck);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("faktum", faktum)
                .append("onValue", onValue)
                .append("skjemaNummer", skjemaNummer)
                .append("property", property).toString();
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
}

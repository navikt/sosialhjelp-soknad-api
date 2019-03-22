package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class RadBeskrivelseBelop implements VedleggRadFrontend {
    public String beskrivelse;
    public Integer belop;

    public RadBeskrivelseBelop withBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public RadBeskrivelseBelop withBelop(Integer belop) {
        this.belop = belop;
        return this;
    }
}

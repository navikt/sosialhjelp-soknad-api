package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class VedleggRadFrontend {
    public String beskrivelse;
    public Integer belop;
    public Integer avdrag;
    public Integer renter;

    public VedleggRadFrontend withBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public VedleggRadFrontend withBelop(Integer belop) {
        this.belop = belop;
        return this;
    }

    public VedleggRadFrontend withAvdrag(Integer avdrag) {
        this.avdrag = avdrag;
        return this;
    }

    public VedleggRadFrontend withRenter(Integer renter) {
        this.renter = renter;
        return this;
    }
}
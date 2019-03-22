package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class RadAvdragRenter implements VedleggRadFrontend {
    public Integer avdrag;
    public Integer renter;

    public RadAvdragRenter withAvdrag(Integer avdrag) {
        this.avdrag = avdrag;
        return this;
    }

    public RadAvdragRenter withRenter(Integer renter) {
        this.renter = renter;
        return this;
    }
}

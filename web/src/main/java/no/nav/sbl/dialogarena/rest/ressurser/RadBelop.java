package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class RadBelop implements VedleggRadFrontend {
    public Integer belop;

    public RadBelop withBelop(Integer belop) {
        this.belop = belop;
        return this;
    }
}

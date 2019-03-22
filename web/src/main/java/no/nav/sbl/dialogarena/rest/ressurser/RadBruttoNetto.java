package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class RadBruttoNetto implements VedleggRadFrontend {
    public Integer brutto;
    public Integer netto;

    public RadBruttoNetto withBrutto(Integer brutto) {
        this.brutto = brutto;
        return this;
    }

    public RadBruttoNetto withNetto(Integer netto) {
        this.netto = netto;
        return this;
    }
}

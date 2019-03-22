package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class RadAlleFelter implements VedleggRadFrontend {
    public String beskrivelse;
    public Integer belop;
    public Integer brutto;
    public Integer netto;
    public Integer avdrag;
    public Integer renter;

    public RadAlleFelter withBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public RadAlleFelter withBelop(Integer belop) {
        this.belop = belop;
        return this;
    }

    public RadAlleFelter withBrutto(Integer brutto) {
        this.brutto = brutto;
        return this;
    }

    public RadAlleFelter withNetto(Integer netto) {
        this.netto = netto;
        return this;
    }

    public RadAlleFelter withAvdrag(Integer avdrag) {
        this.avdrag = avdrag;
        return this;
    }

    public RadAlleFelter withRenter(Integer renter) {
        this.renter = renter;
        return this;
    }
}

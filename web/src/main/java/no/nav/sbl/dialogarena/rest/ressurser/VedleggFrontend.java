package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public final class VedleggFrontend {
    public String type;
    public String gruppe;
    public String beskrivelse;
    public List<Integer> belop;
    public List<Integer> avdrag;
    public List<Integer> renter;
    public String vedleggStatus;
    public List<String> filNavn;

    public VedleggFrontend withType(String type) {
        this.type = type;
        return this;
    }

    public VedleggFrontend withGruppe(String gruppe) {
        this.gruppe = gruppe;
        return this;
    }

    public VedleggFrontend withBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public VedleggFrontend withBelop(List<Integer> belop) {
        this.belop = belop;
        return this;
    }

    public VedleggFrontend withAvdrag(List<Integer> avdrag) {
        this.avdrag = avdrag;
        return this;
    }

    public VedleggFrontend withRenter(List<Integer> renter) {
        this.renter = renter;
        return this;
    }

    public VedleggFrontend withVedleggStatus(String vedleggStatus) {
        this.vedleggStatus = vedleggStatus;
        return this;
    }

    public VedleggFrontend withFilNavn(List<String> filNavn) {
        this.filNavn = filNavn;
        return this;
    }
}
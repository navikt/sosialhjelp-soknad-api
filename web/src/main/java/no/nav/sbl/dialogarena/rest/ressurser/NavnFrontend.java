package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class NavnFrontend {
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
    public String fulltNavn;

    public NavnFrontend() {
    }

    public NavnFrontend(String fornavn, String mellomnavn, String etternavn) {
        this.fornavn = fornavn;
        this.mellomnavn = mellomnavn;
        this.etternavn = etternavn;
        updateFulltNavn();
    }

    private void updateFulltNavn(){
        final String f = this.fornavn != null ? this.fornavn : "";
        final String m = this.mellomnavn != null ? " " + this.mellomnavn : "";
        final String e = this.etternavn != null ? " " + this.etternavn : "";
        this.fulltNavn = f + m + e;
    }

}

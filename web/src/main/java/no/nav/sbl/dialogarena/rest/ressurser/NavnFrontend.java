package no.nav.sbl.dialogarena.rest.ressurser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class NavnFrontend {
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
    public String fulltNavn;

    public void setFornavn(String fornavn) {
        this.fornavn = fornavn;
    }

    public void setMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
    }

    public void setEtternavn(String etternavn) {
        this.etternavn = etternavn;
    }

    public void setFulltNavn(String fulltNavn) {
        this.fulltNavn = fulltNavn;
    }

    public NavnFrontend withFornavn(String fornavn) {
        this.fornavn = fornavn;
        return this;
    }

    public NavnFrontend withMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
        return this;
    }

    public NavnFrontend withEtternavn(String etternavn) {
        this.etternavn = etternavn;
        return this;
    }

    public NavnFrontend withFulltNavn(String fulltNavn) {
        this.fulltNavn = fulltNavn;
        return this;
    }
}

package no.nav.sbl.dialogarena.rest.ressurser;

import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class NavnFrontend {
    public String fornavn;
    public String mellomnavn;
    public String etternavn;
    public String fulltNavn;

    public NavnFrontend(String fornavn, String mellomnavn, String etternavn) {
        this.fornavn = fornavn;
        this.mellomnavn = mellomnavn;
        this.etternavn = etternavn;
        updateFulltNavn();
    }

    private void updateFulltNavn(){
        final String f = !StringUtils.isEmpty(this.fornavn) ? this.fornavn : "";
        final String m = !StringUtils.isEmpty(this.mellomnavn) ? " " + this.mellomnavn : "";
        final String e = !StringUtils.isEmpty(this.etternavn) ? " " + this.etternavn : "";
        this.fulltNavn = (f + m + e).trim();
    }

}

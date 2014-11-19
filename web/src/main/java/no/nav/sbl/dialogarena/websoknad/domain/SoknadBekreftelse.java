package no.nav.sbl.dialogarena.websoknad.domain;

import java.io.Serializable;

public class SoknadBekreftelse implements Serializable {
    private String epost;
    private String temaKode;

    public SoknadBekreftelse() {
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public String getTemaKode() {
        return temaKode;
    }

    public void setTemaKode(String temaKode) {
        this.temaKode = temaKode;
    }

}

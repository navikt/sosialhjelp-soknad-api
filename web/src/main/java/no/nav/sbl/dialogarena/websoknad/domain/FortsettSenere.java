package no.nav.sbl.dialogarena.websoknad.domain;

import java.io.Serializable;

public class FortsettSenere implements Serializable {
    private String epost;

    public FortsettSenere() {
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

}

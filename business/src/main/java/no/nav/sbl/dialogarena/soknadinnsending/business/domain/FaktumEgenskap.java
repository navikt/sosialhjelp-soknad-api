package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.io.Serializable;

public class FaktumEgenskap implements Serializable {
    //public enum FaktumType { FAGREGISTER, BRUKERREGISTRERT; }
    private Long faktumId;
    private Long soknadId;
    private String key;
    private String value;

    public FaktumEgenskap() {
    }

    public FaktumEgenskap(Long soknadId, Long faktumId, String key, String value) {
        this.faktumId = faktumId;
        this.soknadId = soknadId;
        this.key = key;
        this.value = value;
    }


    public Long getFaktumId() {
        return faktumId;
    }

    public void setFaktumId(Long faktumId) {
        this.faktumId = faktumId;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
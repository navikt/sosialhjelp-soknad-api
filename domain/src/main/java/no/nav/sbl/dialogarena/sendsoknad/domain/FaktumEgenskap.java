package no.nav.sbl.dialogarena.sendsoknad.domain;

import java.io.Serializable;


public class FaktumEgenskap implements Serializable {
    private Long faktumId;
    private Long soknadId;
    private String key;
    private String value;
    private Integer systemEgenskap;
    public FaktumEgenskap() {

    }

    public FaktumEgenskap(Long soknadId, Long faktumId, String key, String value, Boolean systemEgenskap) {
        this.faktumId = faktumId;
        this.soknadId = soknadId;
        this.key = key;
        this.value = value;
        this.systemEgenskap = systemEgenskap ? 1 : 0;
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

    public Integer getSystemEgenskap() {
        return systemEgenskap;
    }

    public void setSystemEgenskap(Integer systemEgenskap) {

        this.systemEgenskap = systemEgenskap;
    }

}
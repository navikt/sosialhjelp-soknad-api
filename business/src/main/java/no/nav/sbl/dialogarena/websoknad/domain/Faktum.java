package no.nav.sbl.dialogarena.websoknad.domain;

import java.io.Serializable;

public class Faktum implements Serializable {

    private Long soknadId;
    private String key;
    private String value;

    public String getValue() {
        return value;
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

    public final void setValue(String value) {
        this.value = value;
    }
}
package no.nav.sbl.dialogarena.soknad.domain;

import java.io.Serializable;

public class Faktum implements Serializable {

    private Long soknadId;
    private String key;
    private String value;

    public Object getValue() {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        }
        return value;
    }
    
    public final Long getSoknadId() {
        return soknadId;
    }

    public final void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public final String getKey() {
        return key;
    }

    public final void setKey(String key) {
        this.key = key;
    }

    public final void setValue(String value) {
        this.value = value;
    }
}
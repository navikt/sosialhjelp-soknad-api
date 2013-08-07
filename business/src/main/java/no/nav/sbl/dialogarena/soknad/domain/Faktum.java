package no.nav.sbl.dialogarena.soknad.domain;

import org.joda.time.DateTime;

import java.io.Serializable;

public class Faktum implements Serializable {

    private Long soknadId;
    private String key;
    private String value;
    private DateTime sistEndret;

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

    public void setValue(String value) {
        this.value = value;
    }

    public void setSistEndret(DateTime sistEndret) {
        this.sistEndret = sistEndret;
    }

    public String getValue() {
        return value;
    }
}
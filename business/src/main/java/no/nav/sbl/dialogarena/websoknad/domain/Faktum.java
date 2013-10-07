package no.nav.sbl.dialogarena.websoknad.domain;

import java.io.Serializable;

public class Faktum implements Serializable {

	private Long soknadId;
    private String key;
    private String value;
    private String type;

    public Faktum() {
    	
    }
    
    public Faktum(Long soknadId, String key, String value, String type) {
		super();
		this.soknadId = soknadId;
		this.key = key;
		this.value = value;
		this.type = type;
	}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    @Override
   	public String toString() {
   		return "Faktum [soknadId=" + soknadId + ", key=" + key + ", value="
   				+ value + ", type=" + type + "]";
   	}
}
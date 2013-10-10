package no.nav.sbl.dialogarena.websoknad.domain;

import java.io.Serializable;

import org.joda.time.DateTime;


public class Faktum implements Serializable {

	//public enum FaktumType { FAGREGISTER, BRUKERREGISTRERT; }
	
	private Long faktumId;
	private Long soknadId;
    private String key;
    private String value;
    private String type;
    private DateTime opprettetDato;
    
    public Faktum() {
    	
    }
    
    public Faktum(Long faktumId, Long soknadId, String key, String value, String type, DateTime opprettetDato) {
		this(soknadId, key, value);
		this.faktumId = faktumId;
		this.type = type;
		this.opprettetDato = opprettetDato;
    }

    public Faktum(Long soknadId, String key, String value, String type) {
		this(soknadId,key,value);
		this.type = type;
    }
    


	public Faktum(long soknadId, String key, String value) {
		this.soknadId = soknadId;
		this.key = key;
		this.value = value;
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

	public long getFaktumId() {
		return faktumId;
	}
}
package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.io.Serializable;
import java.util.List;

public class Faktum implements Serializable {

	//public enum FaktumType { FAGREGISTER, BRUKERREGISTRERT; }
	private Long id;
	private Long soknadId;
    private Long vedleggId;
	private String key;
    private String value;
    private List<String> valuelist;
    private String type;
    
	public Faktum() {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVedleggId() {
        return vedleggId;
    }

    public void setVedleggId(Long vedleggId) {
        this.vedleggId = vedleggId;
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

	public List<String> getValuelist() {
		return valuelist;
	}

	public void setValuelist(List<String> valueList) {
		this.valuelist = valueList;
	}

}
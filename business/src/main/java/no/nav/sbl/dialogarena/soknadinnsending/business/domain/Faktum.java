package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.io.Serializable;
import java.util.List;

public class Faktum implements Serializable {

	//public enum FaktumType { FAGREGISTER, BRUKERREGISTRERT; }
	private Long faktumId;
	private Long soknadId;
    private Long vedleggId;
    private Long parrentFaktum;
	private String key;
    private String value;
    private List<Faktum> valuelist;
    private String type;
    
    public Faktum() {
    	
    }
    
	public Faktum(Long soknadId, Long faktumId, String key, String value, String type, Long parrentFaktum) {
		this(soknadId, faktumId, key,value, type);
		this.parrentFaktum = parrentFaktum;
    }
  
    public Faktum(Long soknadId, Long faktumId, String key, String value, String type) {
		this(soknadId, faktumId, key,value);
		this.type = type;
    }
    
	public Faktum(Long soknadId, Long faktumId, String key, String value) {
		this.soknadId = soknadId;
		this.faktumId = faktumId;
		this.key = key;
		this.value = value;
	}

    public Faktum(Long soknadId, String key) {
		this.soknadId = soknadId;
		this.key = key;
	}

	public Long getFaktumId() {
        return faktumId;
    }

    public void setFaktumId(Long faktumId) {
        this.faktumId = faktumId;
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

	public List<Faktum> getValuelist() {
		return valuelist;
	}

	public void setValuelist(List<Faktum> valueList) {
		this.valuelist = valueList;
	}

	public Faktum cloneFaktum() {
		return new Faktum(soknadId, key);
	}

	public Long getParrentFaktum() {
		return parrentFaktum;
	}

	public void setParrentFaktum(Long parrentFaktum) {
		this.parrentFaktum = parrentFaktum;
	}

}
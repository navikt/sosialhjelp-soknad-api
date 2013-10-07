package no.nav.sbl.dialogarena.websoknad.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WebSoknad implements Serializable {

   

	private Long soknadId;
    private String gosysId;
    private String brukerBehandlingId;
    private Map<String, Faktum> fakta;

    public WebSoknad() {
        fakta = new LinkedHashMap<>();
    }

    public final Long getSoknadId() {
        return soknadId;
    }

    public final void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public final String getGosysId() {
        return gosysId;
    }

    public final void setGosysId(String gosysId) {
        this.gosysId = gosysId;
    }

    public final Map<String, Faktum> getFakta() {
        return fakta;
    }

    public final void leggTilFakta(Map<String, Faktum> fakta) {
        this.fakta.putAll(fakta);
    }
    
    public final void leggTilFaktum(String key, Faktum faktum) {
    	this.fakta.put(key, faktum);
    }

	public String getBrukerBehandlingId() {
		return brukerBehandlingId;
	}

	public void setBrukerBehandlingId(String brukerBehandlingId) {
		this.brukerBehandlingId = brukerBehandlingId;
	}

	public long antallFakta() {
		long antallFaktum = 0;
		
		if (fakta != null) {
			antallFaktum = fakta.size();
		}
		
		return antallFaktum;
	}
	
	 @Override
	public String toString() {
		return "WebSoknad [soknadId=" + soknadId + ", gosysId=" + gosysId
				+ ", brukerBehandlingId=" + brukerBehandlingId + ", fakta="
				+ fakta + "]";
	}
}

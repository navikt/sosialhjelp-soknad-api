package no.nav.sbl.dialogarena.websoknad.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WebSoknad implements Serializable {

	private Long soknadId;
    private String gosysId;
    private String brukerBehandlingId;
    private Map<String, Faktum> fakta;
    private SoknadInnsendingStatus status;
	private String aktoerId;
	private DateTime opprettetDato;
    
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

	
	public String getAktoerId() {
		return aktoerId;
	}
	
	@Override
	public String toString() {
		return "WebSoknad [soknadId=" + soknadId + ", gosysId=" + gosysId
				+ ", brukerBehandlingId=" + brukerBehandlingId + ", fakta="
				+ fakta + "]";
	}

	public SoknadInnsendingStatus getStatus() {
		return status;	
	}

	public static WebSoknad startSoknad() {
		// TODO Auto-generated method stub
		return new WebSoknad();
	}

	public WebSoknad medAktorId(String aktorId) {
		this.aktoerId = aktorId;
		return this;
	}

	public WebSoknad medGosysId(String gosysId) {
		this.gosysId = gosysId;
		return this;
	}

	public WebSoknad medBehandlingId(String behandlingsId) {
		this.brukerBehandlingId = behandlingsId;
		return this;
	}
	
	public WebSoknad opprettetDato(DateTime opprettetDato) {
		this.opprettetDato = opprettetDato;
		return this;
	}

	public DateTime getOpprettetDato() {
		return opprettetDato;
	}

	public WebSoknad medId(long id) {
		this.soknadId = id;
		return this;
	}

	public WebSoknad medStatus(SoknadInnsendingStatus status) {
		this.status = status;
		return this;
	}

	public WebSoknad medBrukerData(List<Faktum> hentAlleBrukerData) {
		fakta = new HashMap<String, Faktum>();
		for (Faktum faktum : hentAlleBrukerData) {
			fakta.put(faktum.getKey(), faktum);
		}
		return this;
				
	}


}

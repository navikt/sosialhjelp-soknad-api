package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.ArrayList;
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
    private DelstegStatus delstegStatus;
    
    public DelstegStatus getDelstegStatus() {
        return delstegStatus;
    }

    public void setDelstegStatus(DelstegStatus delstegStatus) {
        this.delstegStatus = delstegStatus;
    }

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

	public Long getOpprettetDato() {
		return opprettetDato.getMillis();
	}

	public WebSoknad medId(long id) {
		this.soknadId = id;
		return this;
	}

	public WebSoknad medStatus(SoknadInnsendingStatus status) {
		this.status = status;
		return this;
	}

	public WebSoknad medBrukerData(List<Faktum> brukerData) {
		fakta = new HashMap<>();
		
		for (Faktum faktum : brukerData) {
			if(faktum.getKey().equals("barn")) {
				faktum = leggBarnTilIValueList(faktum, "barn");
			}else if(faktum.getKey().equals("barnetillegg")) {
				faktum = leggBarnTilIValueList(faktum, "barnetillegg");
			}
			fakta.put(faktum.getKey(), faktum);
		}
		return this;
				
	}

	private Faktum leggBarnTilIValueList(Faktum faktum, String key) {
		String barn = hentBarnJsonMedFaktumId(faktum);
		
		
		
		//TODO: Her må faktumID med i value slik at vi får den med til DOM'en
		
		if(fakta.containsKey(key)) {
			Faktum barneFaktum = fakta.get(key);
			List<String> valueList = barneFaktum.getValuelist();
			valueList.add(barn);
			barneFaktum.setValuelist(valueList);
			return barneFaktum;
		} else {
			List<String> barneliste = new ArrayList<>();
			barneliste.add(barn);
			faktum.setValuelist(barneliste);
			faktum.setValue(null);
			return faktum;
		}
	}

	/**
	 * Småhacky metode for å legge faktumId til i Json-stringen.
	 * 
	 * @param faktum
	 * @return
	 */
    public String hentBarnJsonMedFaktumId(Faktum faktum) {
		String value = faktum.getValue();
		if(value.contains("\"faktumId\"")) {
			return value;
		}
		String aapenJsonString = value.substring(0, value.length()-1);
		String result = aapenJsonString.concat(", \"faktumId\": " + faktum.getFaktumId() + "}");
		return result;
	}

	public WebSoknad medDelstegStatus(DelstegStatus delstegStatus) {
        this.delstegStatus = delstegStatus;
        return this;
    }

}

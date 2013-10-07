package no.nav.sbl.dialogarena.person;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;

@XmlRootElement
public class Adresse {
	
    private Long soknadId;
	private Adressetype type;
	private DateTime gyldigfra;
	private DateTime gyldigtil;
	private String gatenavn;
	private String husnummer;
	private String husbokstav;
	private String postnummer;
	private String poststed;
	private String postboksnavn;
	private String postboksnummer;
	private String adresseeier;
	private List<String> adresseLinjer;
	private String land;
	
	public Adresse(long soknadId, Adressetype type) {
		this.soknadId=soknadId;
		this.type=type;
	}

	public void setGatenavn(String gatenavn) {
		this.gatenavn = gatenavn;
	}
	public String getGatenavn() {
		return gatenavn;
	}
	
	public void setHusnummer(String husnummer) {
		this.husnummer = husnummer;
	}
	public String getHusnummer() {
		return husnummer;
	}
	
	public void setHusbokstav(String husbokstav) {
		this.husbokstav = husbokstav;
	}
	public String getHusbokstav() {
		return husbokstav;
	}
	
	public void setPostnummer(String postnummer) {
		this.postnummer = postnummer;
	}
	public String getPostnummer() {
		return postnummer;
	}

	public long getSoknadId() {
		return soknadId;
	}
	
	public Adressetype getType() {
		return type;
	}

	public void setGyldigfra(DateTime gyldigfra) {
		this.gyldigfra = gyldigfra;
	}
	public DateTime getGyldigFra() {
		return gyldigfra;
	}

	public void setGyldigtil(DateTime gyldigtil) {
		this.gyldigtil = gyldigtil;
	}
	public DateTime getGyldigTil() {
		return gyldigtil;
	}

	public void setPostboksnavn(String postboksnavn) {
		this.postboksnavn = postboksnavn;
	}
	public String getPostboksNavn() {
		return postboksnavn;
	}
	
	public void setPostboksnummer(String postboksnummer) {
		this.postboksnummer = postboksnummer;
	}
	public String getPostboksNummer() {
		return postboksnummer;
	}

	public void setPoststed(String poststed) {
		this.poststed = poststed;
	}
	public String getPoststed() {
		return poststed;
	}

	public void setAdresseeier(String adresseeier) {
		this.adresseeier = adresseeier;
	}
	public String getAdresseEier() {
		return adresseeier;
	}

	public void setUtenlandsadresse(List<String> utenlandsadresse) {
		this.adresseLinjer = utenlandsadresse;
	}
	public List<String> getUtenlandsAdresse() {
		return adresseLinjer;
	}

	public void setLand(String land) {
		this.land = land;
	}
	public String getLand() {
		return land;
	}

}

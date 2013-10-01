package no.nav.sbl.dialogarena.person;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class PersonAdresse {
	private Long soknadId;
	private Adressetype type;
	private String gatenavn;
	private String husnummer;
	private String postnummer;
	
	public PersonAdresse(long soknadId, Adressetype type, String gatenavn, String husnummer, String postnummer) {
		this.soknadId=soknadId;
		this.type=type;
		this.gatenavn=gatenavn;
		this.husnummer=husnummer;
		this.postnummer=postnummer;
	}
	
	public String getGatenavn() {
		return gatenavn;
	}
	
	public String getHusnummer() {
		return husnummer;
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
}

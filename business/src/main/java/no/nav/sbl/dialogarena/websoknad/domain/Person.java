package no.nav.sbl.dialogarena.websoknad.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Person implements Serializable{

	private String fornavn;
	private String etternavn;
	private String mellomnavn;
	private int alder;
	private String postnummer;
	private String poststed;
	private String adresse;
	private String email;
	private String fnr;

	public static Person create() {
		Person p = new Person();
		p.fornavn = "Ola";
		p.etternavn = "Nordmann";
		p.mellomnavn = "J";
		p.alder = 77;
		p.postnummer = "0123";
		p.poststed = "Oslo";
		p.adresse = "Testveien 1";
		p.email = "ola@nordmann.no";
		return p;
	}

	public String getFornavn() {
		return fornavn;
	}

	public void setFornavn(String fornavn) {
		this.fornavn = fornavn;
	}

	public String getEtternavn() {
		return etternavn;
	}

	public void setEtternavn(String etternavn) {
		this.etternavn = etternavn;
	}

	public String getMellomnavn() {
		return mellomnavn;
	}

	public void setMellomnavn(String mellomnavn) {
		this.mellomnavn = mellomnavn;
	}

	public int getAlder() {
		return alder;
	}

	public void setAlder(int alder) {
		this.alder = alder;
	}

	public String getPostnummer() {
		return postnummer;
	}

	public void setPostnummer(String postnummer) {
		this.postnummer = postnummer;
	}

	public String getPoststed() {
		return poststed;
	}

	public void setPoststed(String poststed) {
		this.poststed = poststed;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

    public String getFnr() {
        return fnr;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
    }
}

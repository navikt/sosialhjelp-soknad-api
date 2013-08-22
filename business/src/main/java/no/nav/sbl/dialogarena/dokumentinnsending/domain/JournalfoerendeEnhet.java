package no.nav.sbl.dialogarena.dokumentinnsending.domain;

/**
 * Denne klassen inneholder en kode som indikerer at brukerbehandlingen skal utføres på kontoret for NAV Internasjon. Dette gjelder brukere som har utenlandsadresse.
 */
public enum JournalfoerendeEnhet {

	NAV_INTERNASJONAL("2101");
	
	private String kode;
	
	private JournalfoerendeEnhet(String kode) {
		this.kode = kode;
	}
	
	public String kode() {
		return kode;
	}
}

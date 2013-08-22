package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.sbl.dialogarena.dokumentinnsending.domain.Person;

/**
 * Henter informasjon om en person. I første omgang er vi interessert i brukers adresse og epost.
 * Brukers adresse brukes for å bestemme om brukeren har en utenlandsadresse som en må ta hensyn til 
 * i forhold til videre behandling av søknad. 
 * 
 * @author j139113
 *
 */
public interface PersonService {

	/**
	 * Henter en person fra master for brukerdata. Dersom en ikke finner personen returneres
	 * et person objekt uten noe adresseinformasjon. Dermed feiler ikke den videre innsendingen for bruker.
	 * I tilfeller der dette skjer vil flyten følge normal flyt gjennom systemet.
	 * 
	 * @param ident Identifiserer personen man skal hente informasjon om.
	 * 
	 * @return {@link Person}
	 */
	Person hentPerson(String ident);
	
	/**
	 * Ping tjeneste for BrukerProfil webtjenesten.
	 */
	void ping();
}

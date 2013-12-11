package no.nav.sbl.dialogarena.person;

/**
 * Service for å hente person med familierelasjon fra TPS
 * @author V140448
 *
 */
public interface FamilieRelasjonService {
	/**
	 * Henter person fra TPS og mapper til vårt eget Person-objekt
	 * @param soknadId
	 * @param ident
	 * @return
	 */
    Person hentPerson(Long soknadId, String ident);
}

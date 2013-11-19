package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import static java.lang.Long.parseLong;

/**
 * Genererer behandlingsid basert på applikasjonsid / prefix og databasenøkkel
 */
public class IdGenerator {
	
    public static final String APPLIKASJONSID = "BD07";
    private static final String APPLIKASJON_PREFIX_BASE_36 = "10";

    public static String lagBehandlingsId(long databasenokkel) {
    	Long base = parseLong(APPLIKASJON_PREFIX_BASE_36 + "0000000", 36);
        String behandlingsId = Long.toString(base + databasenokkel, 36).toUpperCase().replace("O", "o").replace("I","i");
        if (!behandlingsId.startsWith(APPLIKASJON_PREFIX_BASE_36)) {
            throw new RuntimeException("Tildelt sekvensrom for behandlingsId er brukt opp. Kan ikke generer behandlingsId " + behandlingsId);
        }
		return behandlingsId;
    }

}

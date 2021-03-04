package no.nav.sosialhjelp.soknad.business.db;

import static java.lang.Long.parseLong;

public final class IdGenerator {

    private IdGenerator() {
    }

    private static final String APPLIKASJON_PREFIX_BASE_36 = "10";

    public static String lagBehandlingsId(long databasenokkel) {
        long base = parseLong(APPLIKASJON_PREFIX_BASE_36 + "0000000", 36);
        String behandlingsId = Long.toString(base + databasenokkel, 36).toUpperCase().replace("O", "o").replace("I","i");
        if (!behandlingsId.startsWith(APPLIKASJON_PREFIX_BASE_36)) {
            throw new RuntimeException("Tildelt sekvensrom for behandlingsId er brukt opp. Kan ikke generer behandlingsId " + behandlingsId);
        }
        return behandlingsId;
    }

}

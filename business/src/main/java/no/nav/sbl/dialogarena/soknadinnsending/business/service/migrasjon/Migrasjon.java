package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public abstract class Migrasjon {
    static int minVersjon;
    static String migrasjonSkjemanummer;

    public Migrasjon(int versjon, String skjemanummer) {
        minVersjon = versjon;
        migrasjonSkjemanummer = skjemanummer;
    }

    public static boolean skalMigrere(String skjemanummer, int versjon) {
        return migrasjonSkjemanummer.equalsIgnoreCase(skjemanummer) && minVersjon-versjon == 1;
    }
    public abstract WebSoknad migreer(WebSoknad soknad, int versjon);
}
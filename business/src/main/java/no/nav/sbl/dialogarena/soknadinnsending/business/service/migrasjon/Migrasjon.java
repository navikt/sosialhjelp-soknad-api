package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public abstract class Migrasjon {
    static int tilVersjon;
    static String migrasjonSkjemanummer;

    public Migrasjon(int nyVersjon, String skjemanummer) {
        tilVersjon = nyVersjon;
        migrasjonSkjemanummer = skjemanummer;
    }

    public static boolean skalMigrere(String skjemanummer, int nyVersjon) {
        return migrasjonSkjemanummer.equalsIgnoreCase(skjemanummer) && tilVersjon - nyVersjon == 1;
    }
    public abstract WebSoknad migrer(WebSoknad soknad, int versjon);
}
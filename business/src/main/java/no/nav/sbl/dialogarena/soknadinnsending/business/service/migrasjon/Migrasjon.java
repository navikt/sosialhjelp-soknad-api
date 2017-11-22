package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public abstract class Migrasjon {
    static int tilVersjon;
    static String migrasjonSkjemanummer;

    public Migrasjon(int tilVersjon, String skjemanummer) {
        Migrasjon.tilVersjon = tilVersjon;
        migrasjonSkjemanummer = skjemanummer;
    }

    public static boolean skalMigrere(int fraVersjon, String skjemanummer) {
        return migrasjonSkjemanummer.equalsIgnoreCase(skjemanummer) && tilVersjon - fraVersjon == 1;
    }
    public abstract WebSoknad migrer(WebSoknad soknad, int fraVersjon);
}
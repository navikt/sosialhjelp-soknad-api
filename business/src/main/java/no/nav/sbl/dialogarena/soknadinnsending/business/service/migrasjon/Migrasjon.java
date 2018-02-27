package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public abstract class Migrasjon {
    static Integer tilVersjon;
    static String migrasjonSkjemanummer;

    public Migrasjon(int tilVersjon, String skjemanummer) {
        Migrasjon.tilVersjon = tilVersjon;
        migrasjonSkjemanummer = skjemanummer;
    }

    public static Integer getTilVersjon() {
        return tilVersjon;
    }

    public static void setTilVersjon(Integer tilVersjon) {
        Migrasjon.tilVersjon = tilVersjon;
    }

    public static String getMigrasjonSkjemanummer() {
        return migrasjonSkjemanummer;
    }

    public static void setMigrasjonSkjemanummer(String migrasjonSkjemanummer) {
        Migrasjon.migrasjonSkjemanummer = migrasjonSkjemanummer;
    }

    public static boolean skalMigrere(Integer fraVersjon, String skjemanummer) {
        return migrasjonSkjemanummer.equalsIgnoreCase(skjemanummer) && tilVersjon - fraVersjon == 1;
    }
    public abstract WebSoknad migrer(Integer fraVersjon, WebSoknad soknad);
}
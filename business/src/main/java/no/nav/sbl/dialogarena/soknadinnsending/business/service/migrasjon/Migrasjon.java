package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public abstract class Migrasjon {
    static Integer tilVersjon;

    public Migrasjon(int tilVersjon) {
        Migrasjon.tilVersjon = tilVersjon;
    }

    public static Integer getTilVersjon() {
        return tilVersjon;
    }

    public static void setTilVersjon(Integer tilVersjon) {
        Migrasjon.tilVersjon = tilVersjon;
    }


    public static boolean skalMigrere(Integer fraVersjon) {
        return tilVersjon - fraVersjon == 1;
    }
    public abstract WebSoknad migrer(Integer fraVersjon, WebSoknad soknad);
}
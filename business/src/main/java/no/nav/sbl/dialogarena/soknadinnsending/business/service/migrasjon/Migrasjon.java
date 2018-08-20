package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

public abstract class Migrasjon {
    Integer tilVersjon;

    public Migrasjon(int tilVersjon) {
        this.tilVersjon = tilVersjon;
    }

    public Integer getTilVersjon() {
        return tilVersjon;
    }

    public boolean skalMigrere(Integer fraVersjon) {
        return tilVersjon - fraVersjon == 1;
    }
    public abstract WebSoknad migrer(Integer fraVersjon, WebSoknad soknad);
}
package no.nav.sbl.dialogarena.soknad.pages.basepage;

import no.nav.sbl.dialogarena.soknad.domain.Soknad;

import java.io.Serializable;

public class BaseViewModel implements Serializable {

    private String tabTittel;
    private Soknad soknad;

    public BaseViewModel(String tabTittel, Soknad soknad) {
        this.tabTittel = tabTittel;
        this.soknad = soknad;
    }

    public final Soknad getSoknad() {
        return soknad;
    }

    public final String getTabTittel() {
        return tabTittel;
    }

    public final String getTittel() {
        return "Tittel!";
    }

    public final Long getSoknadId() {
        return soknad.getSoknadId();
    }
}
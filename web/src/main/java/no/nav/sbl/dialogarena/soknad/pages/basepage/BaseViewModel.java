package no.nav.sbl.dialogarena.soknad.pages.basepage;

import no.nav.sbl.dialogarena.soknad.domain.Soknad;

import java.io.Serializable;

public class BaseViewModel implements Serializable {

    private String tabTittel;
    protected Soknad soknad;

    public BaseViewModel(String tabTittel, Soknad soknad) {
        this.tabTittel = tabTittel;
        this.soknad = soknad;
    }

    public String getTabTittel() {
        return tabTittel;
    }

    public String getTittel() {
        return "Tittel!";
    }

    public Long getSoknadId() {
        return soknad.soknadId;
    }
}

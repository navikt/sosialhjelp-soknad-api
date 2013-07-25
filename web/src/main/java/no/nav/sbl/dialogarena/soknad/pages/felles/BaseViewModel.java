package no.nav.sbl.dialogarena.soknad.pages.felles;

import java.io.Serializable;

public class BaseViewModel implements Serializable {

    private String tabTittel;

    public BaseViewModel(String tabTittel) {
        this.tabTittel = tabTittel;
    }

    public String getTabTittel() {
        return tabTittel;
    }
}

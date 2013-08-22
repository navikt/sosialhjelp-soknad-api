package no.nav.sbl.dialogarena.websoknad.pages.basepage.persondata;

import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.websoknad.pages.basepage.BaseViewModel;

public class PersondataViewModel extends BaseViewModel {
    public PersondataViewModel(String tabTittel, WebSoknad soknad) {
        super(tabTittel, soknad);
    }

    public String getFornavn() {
        return getFaktum("fornavn").getValue();
    }

    public String getEtternavn() {
        return getFaktum("etternavn").getValue();
    }

    public String getFnr() {
        return getFaktum("fnr").getValue();
    }

    public String getAdresse() {
        return getFaktum("adresse").getValue();
    }

    public String getPostnr() {
        return getFaktum("postnr").getValue();
    }

    public String getPoststed() {
        return getFaktum("poststed").getValue();
    }

    public String getNavnLabel() {
        return "Navn";
    }

    public String getFnrLabel() {
        return "Fødselsnummer";
    }

    public String getAdresseLabel() {
        return "Adresse";
    }
}

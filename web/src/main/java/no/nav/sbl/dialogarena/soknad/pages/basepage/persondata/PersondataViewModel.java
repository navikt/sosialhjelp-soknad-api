package no.nav.sbl.dialogarena.soknad.pages.basepage.persondata;

import no.nav.sbl.dialogarena.soknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknad.domain.Soknad;
import no.nav.sbl.dialogarena.soknad.pages.basepage.BaseViewModel;

public class PersondataViewModel extends BaseViewModel {
    public PersondataViewModel(String tabTittel, Soknad soknad) {
        super(tabTittel, soknad);
    }

    public String getFornavn() {
        return getFaktum("fornavn").getValue();
    }

    public String getEtternavn() {
        return getFaktum("etternavn").getValue();
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

    public String getFnr() {
        Faktum fnr = getFaktum("fnr");
        return fnr.getValue();
    }

    public String getAdresse() {
        Faktum adresse = getFaktum("adresse");
        return adresse.getValue();
    }

    public String getPostnr() {
        return getFaktum("postnr").getValue();
    }

    public String getPoststed() {
        return getFaktum("poststed").getValue();
    }
}

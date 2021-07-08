package no.nav.sosialhjelp.soknad.business.service.adressesok;


public class Sokedata {
    public String adresse;
    public String husnummer;
    public String husbokstav;
    public String postnummer;
    public String poststed;

    public String kommunenummer;

    public Sokedata withAdresse(String adresse) {
        this.adresse = adresse;
        return this;
    }

    public Sokedata withHusnummer(String husnummer) {
        this.husnummer = husnummer;
        return this;
    }

    public Sokedata withHusbokstav(String husbokstav) {
        this.husbokstav = husbokstav;
        return this;
    }

    public Sokedata withPostnummer(String postnummer) {
        this.postnummer = postnummer;
        return this;
    }

    public Sokedata withPoststed(String poststed) {
        this.poststed = poststed;
        return this;
    }

    public Sokedata withKommunenummer(String kommunenummer) {
        this.kommunenummer = kommunenummer;
        return this;
    }

    @Override
    public String toString() {
        return "Sokedata [adresse=" + adresse + ", husnummer=" + husnummer + ", husbokstav=" + husbokstav
                + ", postnummer=" + postnummer + ", poststed=" + poststed + ", kommunenummer=" + kommunenummer + "]";
    }
}

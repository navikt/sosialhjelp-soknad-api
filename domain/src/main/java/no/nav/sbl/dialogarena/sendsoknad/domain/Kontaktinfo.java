package no.nav.sbl.dialogarena.sendsoknad.domain;

public class Kontaktinfo {
    private String epostadresse;
    private String mobilnummer;

    public String getEpostadresse() {
        return epostadresse;
    }

    public String getMobilnummer() {
        return mobilnummer;
    }

    public Kontaktinfo withEpostadresse(String epostadresse) {
        this.epostadresse = epostadresse;
        return this;
    }

    public Kontaktinfo withMobilnummer(String mobilnummer) {
        this.mobilnummer = mobilnummer;
        return this;
    }
}

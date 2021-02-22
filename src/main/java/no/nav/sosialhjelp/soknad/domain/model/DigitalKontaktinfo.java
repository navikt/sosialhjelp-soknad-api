package no.nav.sosialhjelp.soknad.domain.model;

public class DigitalKontaktinfo {
    private String epostadresse;
    private String mobilnummer;

    public String getEpostadresse() {
        return epostadresse;
    }

    public String getMobilnummer() {
        return mobilnummer;
    }

    public DigitalKontaktinfo withEpostadresse(String epostadresse) {
        this.epostadresse = epostadresse;
        return this;
    }

    public DigitalKontaktinfo withMobilnummer(String mobilnummer) {
        this.mobilnummer = mobilnummer;
        return this;
    }
}

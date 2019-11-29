package no.nav.sbl.dialogarena.sendsoknad.domain;

public class AdresserOgKontonummer {
    private Adresse folkeregistrertAdresse;
    private String kontonummer;
    private Adresse midlertidigAdresse;

    public Adresse getMidlertidigAdresse() { return midlertidigAdresse; }

    public Adresse getFolkeregistrertAdresse() {
        return folkeregistrertAdresse;
    }

    public String getKontonummer() {
        return kontonummer;
    }

    public AdresserOgKontonummer withFolkeregistrertAdresse(Adresse folkeregistrertAdresse) {
        this.folkeregistrertAdresse = folkeregistrertAdresse;
        return this;
    }

    public AdresserOgKontonummer withKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public AdresserOgKontonummer withMidlertidigAdresse(Adresse midlertidigAdresse) {
        this.midlertidigAdresse = midlertidigAdresse;
        return this;
    }
}
package no.nav.sosialhjelp.soknad.domain.model;

public class Kontonummer {
    private String kontonummer;

    public String getKontonummer() {
        return kontonummer;
    }

    public Kontonummer withKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

}
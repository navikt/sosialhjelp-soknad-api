package no.nav.sosialhjelp.soknad.consumer.person.domain;

public class PersonData {

    private String kontonummer;

    public PersonData withKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public String getKontonummer() {
        return kontonummer;
    }

}